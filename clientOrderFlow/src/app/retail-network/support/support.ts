import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-retail-support',
  imports: [CommonModule, FormsModule],
  templateUrl: './support.html',
  styleUrl: './support.css'
})
export class Support {
  showNewTicketForm: boolean = false;
  selectedTicket: number | null = null;

  newTicket = {
    subject: '',
    category: '',
    priority: '',
    description: ''
  };

  tickets = [
    {
      id: 1,
      subject: 'Проблема с оплатой заказа №12344',
      category: 'Оплата',
      priority: 'high',
      status: 'open',
      statusLabel: 'Открыто',
      date: '20.10.2025',
      lastUpdate: '2 часа назад',
      messages: [
        { from: 'me', text: 'Не могу оплатить заказ через систему. Выдает ошибку "Платеж отклонен"', time: '09:00', date: '20.10.2025' },
        { from: 'support', text: 'Здравствуйте! Мы получили ваше обращение и уже работаем над проблемой. Пожалуйста, попробуйте использовать другой способ оплаты.', time: '09:30', date: '20.10.2025' }
      ]
    },
    {
      id: 2,
      subject: 'Вопрос по доставке',
      category: 'Доставка',
      priority: 'medium',
      status: 'in-progress',
      statusLabel: 'В работе',
      date: '19.10.2025',
      lastUpdate: '1 день назад',
      messages: [
        { from: 'me', text: 'Когда ожидается доставка заказа №12345?', time: '14:00', date: '19.10.2025' },
        { from: 'support', text: 'Ваш заказ находится в пути. Ожидаемая дата доставки - 22.10.2025', time: '14:30', date: '19.10.2025' }
      ]
    },
    {
      id: 3,
      subject: 'Не могу добавить товар в корзину',
      category: 'Технические вопросы',
      priority: 'low',
      status: 'closed',
      statusLabel: 'Закрыто',
      date: '18.10.2025',
      lastUpdate: '2 дня назад',
      messages: [
        { from: 'me', text: 'При попытке добавить товар в корзину ничего не происходит', time: '10:00', date: '18.10.2025' },
        { from: 'support', text: 'Проблема была связана с обновлением системы. Сейчас все работает корректно.', time: '11:00', date: '18.10.2025' }
      ]
    }
  ];

  categories = ['Оплата', 'Доставка', 'Технические вопросы', 'Возврат товара', 'Другое'];
  priorities = [
    { value: 'low', label: 'Низкий' },
    { value: 'medium', label: 'Средний' },
    { value: 'high', label: 'Высокий' }
  ];

  newMessage: string = '';

  toggleNewTicketForm() {
    this.showNewTicketForm = !this.showNewTicketForm;
    if (this.showNewTicketForm) {
      this.selectedTicket = null;
    }
  }

  selectTicket(ticketId: number) {
    this.selectedTicket = ticketId;
    this.showNewTicketForm = false;
  }

  createTicket() {
    if (!this.newTicket.subject || !this.newTicket.category || !this.newTicket.priority || !this.newTicket.description) {
      alert('Пожалуйста, заполните все поля');
      return;
    }

    const ticket = {
      id: this.tickets.length + 1,
      subject: this.newTicket.subject,
      category: this.newTicket.category,
      priority: this.newTicket.priority,
      status: 'open',
      statusLabel: 'Открыто',
      date: new Date().toLocaleDateString('ru-RU'),
      lastUpdate: 'Только что',
      messages: [
        {
          from: 'me',
          text: this.newTicket.description,
          time: new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' }),
          date: new Date().toLocaleDateString('ru-RU')
        }
      ]
    };

    this.tickets.unshift(ticket);
    this.newTicket = { subject: '', category: '', priority: '', description: '' };
    this.showNewTicketForm = false;
    this.selectTicket(ticket.id);
  }

  sendMessage() {
    if (!this.newMessage.trim() || this.selectedTicket === null) return;

    const ticket = this.tickets.find(t => t.id === this.selectedTicket);
    if (ticket) {
      ticket.messages.push({
        from: 'me',
        text: this.newMessage,
        time: new Date().toLocaleTimeString('ru-RU', { hour: '2-digit', minute: '2-digit' }),
        date: new Date().toLocaleDateString('ru-RU')
      });
      ticket.lastUpdate = 'Только что';
    }

    this.newMessage = '';
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
}
