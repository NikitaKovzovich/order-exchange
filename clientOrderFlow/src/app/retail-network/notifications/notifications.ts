import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-retail-notifications',
  imports: [CommonModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class Notifications {
  notifications = [
    {
      id: 1,
      type: 'order',
      title: 'Заказ №12345 доставлен',
      message: 'Ваш заказ от поставщика "Продукты Оптом" был доставлен',
      time: '5 минут назад',
      read: false,
      link: '/retail/orders/12345',
      icon: 'truck'
    },
    {
      id: 2,
      type: 'payment',
      title: 'Требуется оплата',
      message: 'Заказ №12344 ожидает оплаты. Срок оплаты истекает через 2 дня',
      time: '1 час назад',
      read: false,
      link: '/retail/orders/12344',
      icon: 'credit-card'
    },
    {
      id: 3,
      type: 'supplier',
      title: 'Новый поставщик',
      message: 'В базе появился новый поставщик "Мясная Лавка"',
      time: '3 часа назад',
      read: false,
      link: '/retail/suppliers',
      icon: 'user-group'
    },
    {
      id: 4,
      type: 'message',
      title: 'Новое сообщение',
      message: 'Поставщик "Молочная Ферма" ответил на ваш запрос',
      time: '5 часов назад',
      read: true,
      link: '/retail/communications',
      icon: 'chat'
    },
    {
      id: 5,
      type: 'order',
      title: 'Заказ подтвержден',
      message: 'Заказ №12343 подтвержден поставщиком и готовится к отправке',
      time: 'Вчера',
      read: true,
      link: '/retail/orders/12343',
      icon: 'check-circle'
    }
  ];

  filteredNotifications = [...this.notifications];
  filter: string = 'all';

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  applyFilter(filter: string) {
    this.filter = filter;
    if (filter === 'all') {
      this.filteredNotifications = [...this.notifications];
    } else if (filter === 'unread') {
      this.filteredNotifications = this.notifications.filter(n => !n.read);
    } else {
      this.filteredNotifications = this.notifications.filter(n => n.type === filter);
    }
  }

  markAsRead(notificationId: number) {
    const notification = this.notifications.find(n => n.id === notificationId);
    if (notification) {
      notification.read = true;
    }
  }

  markAllAsRead() {
    this.notifications.forEach(n => n.read = true);
  }

  deleteNotification(notificationId: number) {
    const index = this.notifications.findIndex(n => n.id === notificationId);
    if (index > -1) {
      this.notifications.splice(index, 1);
      this.applyFilter(this.filter);
    }
  }

  getIconPath(icon: string): string {
    const icons: { [key: string]: string } = {
      'truck': 'M9 17a2 2 0 11-4 0 2 2 0 014 0zM19 17a2 2 0 11-4 0 2 2 0 014 0z M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a1 1 0 001 1h1M5 17a2 2 0 104 0m-4 0a2 2 0 114 0m6 0a2 2 0 104 0m-4 0a2 2 0 114 0',
      'credit-card': 'M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z',
      'user-group': 'M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z',
      'chat': 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z',
      'check-circle': 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z'
    };
    return icons[icon] || icons['check-circle'];
  }

  getTypeColor(type: string): string {
    const colors: { [key: string]: string } = {
      'order': 'bg-blue-100 text-blue-600',
      'payment': 'bg-yellow-100 text-yellow-600',
      'supplier': 'bg-green-100 text-green-600',
      'message': 'bg-purple-100 text-purple-600'
    };
    return colors[type] || 'bg-gray-100 text-gray-600';
  }
}
