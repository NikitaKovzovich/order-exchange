import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SupportService } from '../../services/support.service';
import { CreateTicketRequest, SupportTicket, TicketCategory } from '../../models/api.models';

interface Ticket {
  id: number;
  subject: string;
  description: string;
  category: string;
  status: string;
  date: string;
}

@Component({
  selector: 'app-support',
  imports: [CommonModule, FormsModule],
  templateUrl: './support.html',
  styleUrl: './support.css'
})
export class Support implements OnInit {
  showNewTicketForm: boolean = false;

  newTicket = {
    subject: '',
    category: '',
    description: ''
  };

  tickets: Ticket[] = [];

  constructor(
    private supportService: SupportService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadTickets();
  }

  submitTicket() {
    if (this.newTicket.subject && this.newTicket.category && this.newTicket.description) {
      const request: CreateTicketRequest = {
        subject: this.newTicket.subject,
        message: this.newTicket.description,
        category: this.mapCategory(this.newTicket.category),
        priority: 'NORMAL'
      };

      this.supportService.createTicket(request).subscribe({
        next: ticket => {
          this.tickets.unshift(this.mapTicket(ticket));
          this.newTicket = {
            subject: '',
            category: '',
            description: ''
          };
          this.showNewTicketForm = false;
          this.router.navigate(['/supplier/support', ticket.id]);
        },
        error: error => console.error('Error creating ticket:', error)
      });
    }
  }

  selectTicket(ticket: Ticket) {
    this.router.navigate(['/supplier/support', ticket.id]);
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'open': 'Открыто',
      'answered': 'Получен ответ',
      'closed': 'Закрыто'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'open': 'bg-blue-200 text-blue-600',
      'answered': 'bg-green-200 text-green-600',
      'closed': 'bg-gray-200 text-gray-600',
      'in_progress': 'bg-yellow-200 text-yellow-700'
    };
    return classes[status] || 'bg-gray-200 text-gray-600';
  }

  private loadTickets(): void {
    this.supportService.getUserTickets().subscribe({
      next: page => {
        this.tickets = page.content.map(ticket => this.mapTicket(ticket));
      },
      error: error => console.error('Error loading tickets:', error)
    });
  }

  private mapTicket(ticket: SupportTicket): Ticket {
    return {
      id: ticket.id,
      subject: ticket.subject,
      description: ticket.lastMessage || 'Без сообщений',
      category: ticket.category,
      status: this.mapStatus(ticket.status),
      date: new Date(ticket.createdAt).toLocaleDateString('ru-RU')
    };
  }

  private mapCategory(category: string): TicketCategory {
    switch (category) {
      case 'technical':
        return 'TECHNICAL_ISSUE';
      case 'payment':
        return 'PAYMENT_ISSUE';
      case 'delivery':
        return 'ORDER_ISSUE';
      default:
        return 'OTHER';
    }
  }

  private mapStatus(status: string): string {
    switch (status) {
      case 'OPEN':
        return 'open';
      case 'RESOLVED':
      case 'WAITING_USER':
        return 'answered';
      case 'IN_PROGRESS':
        return 'in_progress';
      case 'CLOSED':
        return 'closed';
      default:
        return status.toLowerCase();
    }
  }
}
