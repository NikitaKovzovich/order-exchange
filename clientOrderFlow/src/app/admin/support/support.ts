import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { SupportService } from '../../services/support.service';
import { SupportTicket, TicketStatus } from '../../models/api.models';

@Component({
  selector: 'admin-support',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './support.html',
  styleUrls: ['./support.css']
})
export class Support implements OnInit {
  searchQuery: string = '';
  selectedStatus: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';

  tickets: SupportTicket[] = [];
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  readonly pageSize: number = 20;

  constructor(private supportService: SupportService) {}

  ngOnInit() {
    this.loadTickets();
  }

  loadTickets() {
    this.isLoading = true;
    this.errorMessage = '';

    this.supportService.getAdminTickets(this.selectedStatus || undefined, this.searchQuery || undefined, this.currentPage, this.pageSize).subscribe({
      next: (page) => {
        this.tickets = page.content;
        this.currentPage = page.number;
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tickets:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить обращения';
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadTickets();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadTickets();
  }

  goToPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }

    this.currentPage -= 1;
    this.loadTickets();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.currentPage += 1;
    this.loadTickets();
  }

  assignTicket(ticketId: number) {
    this.supportService.assignTicket(ticketId).subscribe({
      next: () => this.loadTickets(),
      error: (error) => console.error('Error assigning ticket:', error)
    });
  }

  getStatusLabel(status: TicketStatus): string {
    const labels: { [key: string]: string } = {
      'OPEN': 'Открыт',
      'IN_PROGRESS': 'В работе',
      'WAITING_USER': 'Ожидает ответа',
      'RESOLVED': 'Решен',
      'CLOSED': 'Закрыт'
    };
    return labels[status] || status;
  }

  getStatusClass(status: TicketStatus): string {
    const classes: { [key: string]: string } = {
      'OPEN': 'bg-green-100 text-green-800',
      'IN_PROGRESS': 'bg-blue-100 text-blue-800',
      'WAITING_USER': 'bg-yellow-100 text-yellow-800',
      'RESOLVED': 'bg-indigo-100 text-indigo-800',
      'CLOSED': 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  get rangeStart(): number {
    if (this.totalElements === 0) {
      return 0;
    }

    return this.currentPage * this.pageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }
}
