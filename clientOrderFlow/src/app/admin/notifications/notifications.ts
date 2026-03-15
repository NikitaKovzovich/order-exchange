import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Header } from '../shared/header/header';
import { UserNotification } from '../../models/api.models';
import { UserNotificationService } from '../../services/user-notification.service';

interface Notification extends UserNotification {}

@Component({
  selector: 'admin-notifications',
  standalone: true,
  imports: [CommonModule, Header],
  templateUrl: './notifications.html',
  styleUrls: ['./notifications.css']
})
export class Notifications implements OnInit {
  notifications: Notification[] = [];
  isLoading: boolean = false;
  unreadCount: number = 0;

  constructor(private userNotificationService: UserNotificationService) {}

  ngOnInit() {
    this.loadNotifications();
  }

  loadNotifications() {
    this.isLoading = true;

    this.userNotificationService.getNotifications(0, 50).subscribe({
      next: page => {
        this.notifications = page.content;
        this.unreadCount = page.content.filter(notification => !notification.isRead).length;
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading admin notifications:', error);
        this.notifications = [];
        this.unreadCount = 0;
        this.isLoading = false;
      }
    });
  }

  markAsRead(notification: Notification) {
    if (notification.isRead) {
      return;
    }

    this.userNotificationService.markAsRead(notification.id).subscribe({
      next: () => {
        notification.isRead = true;
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      },
      error: error => console.error('Error marking admin notification as read:', error)
    });
  }

  markAllAsRead() {
    this.userNotificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map(notification => ({
          ...notification,
          isRead: true
        }));
        this.unreadCount = 0;
      },
      error: error => console.error('Error marking all admin notifications as read:', error)
    });
  }
}

