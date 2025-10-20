import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';

interface SupportTicket {
  id: number;
  title: string;
  subject: string;
  company: string;
  userName: string;
  status: string;
  createdAt: string;
}

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

  tickets: SupportTicket[] = [
    { id: 1, title: 'Проблема с оплатой', subject: 'Проблема с оплатой', company: 'БелПродукт', userName: 'ООО "БелПродукт"', status: 'open', createdAt: '2024-10-18' },
    { id: 2, title: 'Вопрос по доставке', subject: 'Вопрос по доставке', company: 'Евроопт', userName: 'Евроопт', status: 'open', createdAt: '2024-10-17' },
    { id: 3, title: 'Ошибка в заказе', subject: 'Ошибка в заказе', company: 'ИП Иванов', userName: 'ИП Иванов', status: 'closed', createdAt: '2024-10-16' }
  ];

  filteredTickets: SupportTicket[] = [];

  ngOnInit() {
    this.filteredTickets = this.tickets;
  }

  applyFilters() {
    this.filteredTickets = this.tickets.filter(ticket => {
      const matchesSearch = !this.searchQuery ||
        ticket.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        ticket.company.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus = this.selectedStatus === 'all' || ticket.status === this.selectedStatus;

      return matchesSearch && matchesStatus;
    });
  }

  getStatusLabel(status: string): string {
    return status === 'open' ? 'Открыто' : 'Закрыто';
  }

  getStatusClass(status: string): string {
    return status === 'open'
      ? 'bg-green-100 text-green-800'
      : 'bg-gray-100 text-gray-800';
  }
}
