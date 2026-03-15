import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { SupportService } from '../../services/support.service';
import { SupportTicket as SupportTicketModel, TicketMessage, TicketStatus } from '../../models/api.models';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'admin-support-ticket',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './support-ticket.html',
  styleUrls: ['./support-ticket.css']
})
export class SupportTicket implements OnInit {
  ticketId: number = 0;
  ticket: SupportTicketModel | null = null;
  newMessage: string = '';
  messages: TicketMessage[] = [];
  isLoading: boolean = false;
  isProcessing: boolean = false;
  notification: UiNotification | null = null;

  constructor(
    private route: ActivatedRoute,
    private supportService: SupportService
  ) {}

  ngOnInit() {
    this.ticketId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadTicket();
  }

  sendMessage() {
    if (!this.newMessage.trim()) {
      return;
    }

    this.isProcessing = true;
    this.supportService.sendTicketMessage(this.ticketId, {
      message: this.newMessage,
      isInternalNote: false
    }).subscribe({
      next: message => {
        this.messages.push(message);
        this.newMessage = '';
        this.isProcessing = false;
        this.loadTicket();
      },
      error: error => {
        console.error('Error sending admin ticket message:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось отправить сообщение.'
        };
        this.isProcessing = false;
      }
    });
  }

  assignTicket(): void {
    this.runAction(() => this.supportService.assignTicket(this.ticketId), 'Обращение назначено на администратора.');
  }

  resolveTicket(): void {
    this.runAction(() => this.supportService.resolveTicket(this.ticketId), 'Обращение отмечено как решённое.');
  }

  closeTicket(): void {
    this.runAction(() => this.supportService.closeTicket(this.ticketId), 'Обращение закрыто.');
  }

  reopenTicket(): void {
    this.runAction(() => this.supportService.reopenTicket(this.ticketId), 'Обращение переоткрыто.');
  }

  getStatusLabel(status?: TicketStatus): string {
    switch (status) {
      case 'OPEN':
        return 'Открыт';
      case 'IN_PROGRESS':
        return 'В работе';
      case 'WAITING_USER':
        return 'Ожидает ответа';
      case 'RESOLVED':
        return 'Решён';
      case 'CLOSED':
        return 'Закрыт';
      default:
        return '—';
    }
  }

  getStatusClass(status?: TicketStatus): string {
    switch (status) {
      case 'OPEN':
        return 'bg-green-100 text-green-800';
      case 'IN_PROGRESS':
        return 'bg-blue-100 text-blue-800';
      case 'WAITING_USER':
        return 'bg-yellow-100 text-yellow-800';
      case 'RESOLVED':
        return 'bg-indigo-100 text-indigo-800';
      case 'CLOSED':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDateTime(value?: string): string {
    return value ? new Date(value).toLocaleString('ru-RU') : '—';
  }

  isAdminMessage(message: TicketMessage): boolean {
    return message.senderRole === 'ADMIN';
  }

  private loadTicket(): void {
    this.isLoading = true;

    this.supportService.getTicketById(this.ticketId).subscribe({
      next: ticket => {
        this.ticket = ticket;
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading admin ticket:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось загрузить обращение.'
        };
        this.isLoading = false;
      }
    });

    this.supportService.getTicketMessages(this.ticketId).subscribe({
      next: messages => {
        this.messages = messages;
      },
      error: error => {
        console.error('Error loading admin ticket messages:', error);
      }
    });
  }

  private runAction(action: () => ReturnType<SupportService['assignTicket']>, successMessage: string): void {
    this.isProcessing = true;
    action().subscribe({
      next: ticket => {
        this.ticket = ticket;
        this.notification = {
          type: 'success',
          message: successMessage
        };
        this.isProcessing = false;
        this.loadTicket();
      },
      error: error => {
        console.error('Error performing admin ticket action:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось выполнить действие по обращению.'
        };
        this.isProcessing = false;
      }
    });
  }
}
