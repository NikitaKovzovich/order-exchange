import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  tickets: Ticket[] = [
    {
      id: 1001,
      subject: 'Проблема с загрузкой каталога',
      description: 'Не могу загрузить новые товары в каталог, появляется ошибка',
      category: 'Технические проблемы',
      status: 'open',
      date: '30.09.2025'
    },
    {
      id: 1000,
      subject: 'Вопрос по комиссии',
      description: 'Уточните размер комиссии за транзакции',
      category: 'Вопросы по оплате',
      status: 'answered',
      date: '28.09.2025'
    },
    {
      id: 999,
      subject: 'Изменение реквизитов',
      description: 'Необходимо обновить банковские реквизиты',
      category: 'Другое',
      status: 'closed',
      date: '25.09.2025'
    }
  ];

  ngOnInit() {
  }

  submitTicket() {
    if (this.newTicket.subject && this.newTicket.category && this.newTicket.description) {
      const ticket: Ticket = {
        id: this.tickets.length > 0 ? Math.max(...this.tickets.map(t => t.id)) + 1 : 1,
        subject: this.newTicket.subject,
        description: this.newTicket.description,
        category: this.newTicket.category,
        status: 'open',
        date: new Date().toLocaleDateString('ru-RU')
      };

      this.tickets.unshift(ticket);

      this.newTicket = {
        subject: '',
        category: '',
        description: ''
      };

      this.showNewTicketForm = false;
    }
  }

  selectTicket(ticket: Ticket) {
    console.log('Selected ticket:', ticket);
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
      'closed': 'bg-gray-200 text-gray-600'
    };
    return classes[status] || 'bg-gray-200 text-gray-600';
  }
}
