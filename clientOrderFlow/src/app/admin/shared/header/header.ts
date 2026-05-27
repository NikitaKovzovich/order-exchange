import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterModule } from '@angular/router';
import { filter } from 'rxjs/operators';
import { UserNotificationService } from '../../../services/user-notification.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'admin-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class Header implements OnInit {
  @Input() title: string = 'Панель администратора';

  hasNotifications: boolean = false;

  constructor(
    private router: Router,
    private userNotificationService: UserNotificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.refreshUnread();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.refreshUnread());
  }

  private refreshUnread() {
    if (!this.authService.getToken()) {
      this.hasNotifications = false;
      return;
    }
    this.userNotificationService.getUnreadCount().subscribe({
      next: count => this.hasNotifications = count > 0,
      error: () => this.hasNotifications = false
    });
  }

  get userInitial(): string {
    return 'A';
  }
}

