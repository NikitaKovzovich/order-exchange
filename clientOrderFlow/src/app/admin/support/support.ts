import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { ChatService } from '../../services/chat.service';
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
  selectedStatus: string = 'all';
  isLoading: boolean = false;

  tickets: SupportTicket[] = [];
  filteredTickets: SupportTicket[] = [];

  constructor(private chatService: ChatService) {}

  ngOnInit() {
    this.loadTickets();
  }

  loadTickets() {
    this.isLoading = true;
    this.chatService.getAllTickets().subscribe({
      next: (tickets) => {
        this.tickets = tickets;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading tickets:', error);
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    this.filteredTickets = this.tickets.filter(ticket => {
      const matchesSearch = !this.searchQuery ||
        ticket.subject.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        ticket.ticketNumber.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus = this.selectedStatus === 'all' || ticket.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }

  assignTicket(ticketId: number) {
    this.chatService.assignTicket(ticketId).subscribe({
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
}
