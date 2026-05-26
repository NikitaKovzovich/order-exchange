import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Header } from '../shared/header/header';
import { UserNotification } from '../../models/api.models';
import { UserNotificationService } from '../../services/user-notification.service';

interface AdminNotificationView {
  id: number;
  type: string;
  category: string;
  title: string;
  message: string;
  time: string;
  read: boolean;
  icon: string;
}

@Component({
  selector: 'admin-notifications',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.css']
})
export class Notifications implements OnInit {
  notifications: AdminNotificationView[] = [];
  filteredNotifications: AdminNotificationView[] = [];
  filter: string = 'all';
  isLoading: boolean = false;

  constructor(private userNotificationService: UserNotificationService) {}

  ngOnInit() {
    this.loadNotifications();
  }

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
      this.filteredNotifications = this.notifications.filter(n => n.category === filter);
    }
  }

  markAsRead(notification: AdminNotificationView) {
    if (notification.read) {
      return;
    }
    this.userNotificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.read = true;
        this.applyFilter(this.filter);
      },
      error: error => console.error('Error marking admin notification as read:', error)
    });
  }

  markAllAsRead() {
    this.userNotificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
        this.applyFilter(this.filter);
      },
      error: error => console.error('Error marking all admin notifications as read:', error)
    });
  }

  getIconPath(icon: string): string {
    const icons: { [key: string]: string } = {
      'verification': 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      'support': 'M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z',
      'system': 'M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z M15 12a3 3 0 11-6 0 3 3 0 016 0z'
    };
    return icons[icon] || icons['system'];
  }

  getTypeColor(category: string): string {
    const colors: { [key: string]: string } = {
      'verification': 'bg-green-100 text-green-600',
      'support': 'bg-purple-100 text-purple-600',
      'system': 'bg-blue-100 text-blue-600'
    };
    return colors[category] || 'bg-gray-100 text-gray-600';
  }

  private loadNotifications() {
    this.isLoading = true;
    this.userNotificationService.getNotifications(0, 50).subscribe({
      next: page => {
        this.notifications = page.content.map(notification => this.mapNotification(notification));
        this.filteredNotifications = [...this.notifications];
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading admin notifications:', error);
        this.notifications = [];
        this.filteredNotifications = [];
        this.isLoading = false;
      }
    });
  }

  private mapNotification(notification: UserNotification): AdminNotificationView {
    const rawType = (notification.type || '').toUpperCase();
    let category = 'system';
    if (rawType.includes('VERIFICATION') || rawType.includes('REGISTRATION')) {
      category = 'verification';
    } else if (rawType.includes('TICKET') || rawType.includes('SUPPORT')) {
      category = 'support';
    }

    return {
      id: notification.id,
      type: notification.type,
      category,
      title: notification.title || 'Уведомление',
      message: notification.message,
      time: new Date(notification.createdAt).toLocaleString('ru-RU'),
      read: notification.isRead,
      icon: category
    };
  }
}
