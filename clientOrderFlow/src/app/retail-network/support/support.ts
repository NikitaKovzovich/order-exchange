import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SupportService } from '../../services/support.service';
import { CreateTicketRequest, SendTicketMessageRequest, SupportTicket, TicketCategory, TicketMessage, TicketPriority } from '../../models/api.models';

@Component({
  selector: 'app-retail-support',
  imports: [CommonModule, FormsModule],
  templateUrl: './support.html',
  styleUrl: './support.css'
})
export class Support {
  showNewTicketForm: boolean = false;
  selectedTicket: number | null = null;
  formError: string = '';

  newTicket = {
    subject: '',
    category: '',
    priority: '',
    description: ''
  };

  tickets: Array<{
    id: number;
    subject: string;
    category: string;
    priority: string;
    status: string;
    statusLabel: string;
    date: string;
    lastUpdate: string;
    messages: Array<{ from: 'me' | 'support'; text: string; time: string; date: string }>;
  }> = [];

  categories = ['Оплата', 'Доставка', 'Технические вопросы', 'Возврат товара', 'Другое'];
  priorities = [
    { value: 'low', label: 'Низкий' },
    { value: 'medium', label: 'Средний' },
    { value: 'high', label: 'Высокий' }
  ];

  newMessage: string = '';

  constructor(private supportService: SupportService) {
    this.loadTickets();
  }

  toggleNewTicketForm() {
    this.showNewTicketForm = !this.showNewTicketForm;
    if (this.showNewTicketForm) {
      this.selectedTicket = null;
    }
  }

  createTicket() {
    if (!this.newTicket.subject || !this.newTicket.category || !this.newTicket.priority || !this.newTicket.description) {
      this.formError = 'Пожалуйста, заполните все поля.';
      return;
    }

    this.formError = '';

    const request: CreateTicketRequest = {
      subject: this.newTicket.subject,
      message: this.newTicket.description,
      category: this.mapCategory(this.newTicket.category),
      priority: this.mapPriority(this.newTicket.priority)
    };

    this.supportService.createTicket(request).subscribe({
      next: ticket => {
        const mapped = this.mapTicket(ticket, []);
        this.tickets.unshift(mapped);
        this.newTicket = { subject: '', category: '', priority: '', description: '' };
        this.formError = '';
        this.showNewTicketForm = false;
        this.selectTicket(mapped.id);
      },
      error: error => console.error('Error creating retail ticket:', error)
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || this.selectedTicket === null) return;

    const ticketId = this.selectedTicket;
    const request: SendTicketMessageRequest = {
      message: this.newMessage,
      isInternalNote: false
    };

    this.supportService.sendTicketMessage(ticketId, request).subscribe({
      next: message => {
        const ticket = this.tickets.find(t => t.id === ticketId);
        if (ticket) {
          ticket.messages.push(this.mapMessage(message));
          ticket.lastUpdate = 'Только что';
        }
        this.newMessage = '';
      },
      error: error => console.error('Error sending ticket message:', error)
    });
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'open': 'bg-blue-100 text-blue-800',
      'in-progress': 'bg-yellow-100 text-yellow-800',
      'closed': 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  getPriorityClass(priority: string): string {
    const classes: { [key: string]: string } = {
      'low': 'bg-green-100 text-green-800',
      'medium': 'bg-yellow-100 text-yellow-800',
      'high': 'bg-red-100 text-red-800'
    };
    return classes[priority] || 'bg-gray-100 text-gray-800';
  }

  getPriorityLabel(priority: string): string {
    const labels: { [key: string]: string } = {
      'low': 'Низкий',
      'medium': 'Средний',
      'high': 'Высокий'
    };
    return labels[priority] || priority;
  }

  get selectedTicketData() {
    return this.tickets.find(t => t.id === this.selectedTicket);
  }

  private loadTickets(): void {
    this.supportService.getUserTickets().subscribe({
      next: page => {
        const tickets = page.content;
        this.tickets = tickets.map(ticket => this.mapTicket(ticket, []));
        if (tickets.length > 0 && this.selectedTicket === null) {
          this.selectTicket(tickets[0].id);
        }
      },
      error: error => console.error('Error loading retail tickets:', error)
    });
  }

  private loadTicketMessages(ticketId: number): void {
    this.supportService.getTicketMessages(ticketId).subscribe({
      next: messages => {
        const ticket = this.tickets.find(item => item.id === ticketId);
        if (ticket) {
          ticket.messages = messages.map(message => this.mapMessage(message));
        }
      },
      error: error => console.error('Error loading ticket messages:', error)
    });
  }

  private mapTicket(ticket: SupportTicket, messages: TicketMessage[]) {
    return {
      id: ticket.id,
      subject: ticket.subject,
      category: this.getCategoryLabel(ticket.category),
      priority: this.getPriorityValue(ticket.priority),
      status: this.getStatusValue(ticket.status),
      statusLabel: this.getStatusLabel(ticket.status),
      date: new Date(ticket.createdAt).toLocaleDateString('ru-RU'),
      lastUpdate: ticket.updatedAt ? new Date(ticket.updatedAt).toLocaleString('ru-RU') : '—',
      messages: messages.map(message => this.mapMessage(message))
    };
  }

  private mapMessage(message: TicketMessage) {
    return {
      from: message.senderRole === 'ADMIN' ? 'support' as const : 'me' as const,
      text: message.messageText,
      time: new Date(message.createdAt).toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' }),
      date: new Date(message.createdAt).toLocaleDateString('ru-RU')
    };
  }

  private mapCategory(category: string): TicketCategory {
    switch (category) {
      case 'Оплата':
        return 'PAYMENT_ISSUE';
      case 'Доставка':
        return 'ORDER_ISSUE';
      case 'Технические вопросы':
        return 'TECHNICAL_ISSUE';
      default:
        return 'OTHER';
    }
  }

  private mapPriority(priority: string): TicketPriority {
    switch (priority) {
      case 'high':
        return 'HIGH';
      case 'medium':
        return 'NORMAL';
      default:
        return 'LOW';
    }
  }

  private getStatusValue(status: string): string {
    switch (status) {
      case 'OPEN':
        return 'open';
      case 'IN_PROGRESS':
        return 'in-progress';
      case 'CLOSED':
        return 'closed';
      case 'RESOLVED':
        return 'closed';
      default:
        return status.toLowerCase();
    }
  }

  private getStatusLabel(status: string): string {
    switch (status) {
      case 'OPEN':
        return 'Открыто';
      case 'IN_PROGRESS':
        return 'В работе';
      case 'WAITING_USER':
        return 'Нужен ответ';
      case 'RESOLVED':
        return 'Решено';
      case 'CLOSED':
        return 'Закрыто';
      default:
        return status;
    }
  }

  private getPriorityValue(priority: string): string {
    switch (priority) {
      case 'HIGH':
        return 'high';
      case 'NORMAL':
        return 'medium';
      default:
        return 'low';
    }
  }

  private getCategoryLabel(category: string): string {
    switch (category) {
      case 'PAYMENT_ISSUE':
        return 'Оплата';
      case 'ORDER_ISSUE':
        return 'Доставка';
      case 'TECHNICAL_ISSUE':
        return 'Технические вопросы';
      default:
        return 'Другое';
    }
  }

  selectTicket(ticketId: number) {
    this.selectedTicket = ticketId;
    this.showNewTicketForm = false;
    this.loadTicketMessages(ticketId);
  }
}
