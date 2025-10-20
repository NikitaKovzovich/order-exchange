import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Header } from '../shared/header/header';

interface Notification {
  id: number;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}

@Component({
  selector: 'admin-notifications',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.css']
})
export class Notifications implements OnInit {
  notifications: Notification[] = [];

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.notifications = [
      { id: 1, title: 'Новая заявка на верификацию', message: 'ИП Иванов А.А. подал з��явку на верификацию', type: 'verification', read: false, createdAt: '2024-10-18 10:30' },
      { id: 2, title: 'Новое обращение в поддержку', message: 'ООО "БелПродукт" создал обращение', type: 'support', read: false, createdAt: '2024-10-18 09:15' },
      { id: 3, title: 'Новый пользователь', message: 'Зарегистрирован новый пользователь', type: 'user', read: true, createdAt: '2024-10-17 14:20' }
    ];
  }

  markAsRead(notification: Notification) {
    notification.read = true;
  }

  markAllAsRead() {
    this.notifications.forEach(n => n.read = true);
  }
}

