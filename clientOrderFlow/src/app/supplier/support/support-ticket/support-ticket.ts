import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SupportService } from '../../../services/support.service';
import { SupportTicket as SupportTicketModel, TicketMessage, TicketStatus } from '../../../models/api.models';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'supplier-support-ticket',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './support-ticket.html',
  styleUrls: ['./support-ticket.css']
})
export class SupportTicket implements OnInit {
  ticketId: number = 0;
  ticket: SupportTicketModel | null = null;
  messages: TicketMessage[] = [];
  newMessage: string = '';
  isLoading: boolean = false;
  isProcessing: boolean = false;
  notification: UiNotification | null = null;

  constructor(
    private route: ActivatedRoute,
    private supportService: SupportService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.notification = {
        type: 'error',
        message: 'Некорректный идентификатор обращения.'
      };
      return;
    }

    this.ticketId = id;
    this.loadTicket();
  }

  sendMessage(): void {
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
        this.notification = {
          type: 'success',
          message: 'Сообщение отправлено.'
        };
        this.isProcessing = false;
        this.loadTicket();
      },
      error: error => {
        console.error('Error sending supplier ticket message:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось отправить сообщение.'
        };
        this.isProcessing = false;
      }
    });
  }

  closeTicket(): void {
    this.runAction(() => this.supportService.closeTicket(this.ticketId), 'Обращение закрыто.');
  }

  reopenTicket(): void {
    this.runAction(() => this.supportService.reopenTicket(this.ticketId), 'Обращение снова открыто.');
  }

  getStatusLabel(status?: TicketStatus): string {
    switch (status) {
      case 'OPEN':
        return 'Открыт';
      case 'IN_PROGRESS':
        return 'В работе';
      case 'WAITING_USER':
        return 'Нужен ваш ответ';
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
        return 'bg-blue-100 text-blue-700';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-700';
      case 'WAITING_USER':
        return 'bg-amber-100 text-amber-700';
      case 'RESOLVED':
        return 'bg-green-100 text-green-700';
      case 'CLOSED':
        return 'bg-gray-100 text-gray-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  formatDateTime(value?: string): string {
    return value ? new Date(value).toLocaleString('ru-RU') : '—';
  }

  isOwnMessage(message: TicketMessage): boolean {
    return message.senderRole !== 'ADMIN';
  }

  private loadTicket(): void {
    this.isLoading = true;

    this.supportService.getTicketById(this.ticketId).subscribe({
      next: ticket => {
        this.ticket = ticket;
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading supplier ticket:', error);
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
        console.error('Error loading supplier ticket messages:', error);
      }
    });
  }

  private runAction(action: () => ReturnType<SupportService['closeTicket']>, successMessage: string): void {
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
        console.error('Error performing supplier ticket action:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось выполнить действие по обращению.'
        };
        this.isProcessing = false;
      }
    });
  }
}

