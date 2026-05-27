import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { NotificationService } from '../../../services/notification.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [CommonModule, RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header implements OnInit {
  @Input() pageTitle?: string;

  resolvedTitle: string = 'Главная панель';
  companyName: string = 'Поставщик';
  companyLogo: string = 'https://placehold.co/100x100/E2E8F0/4A5568?text=Лого';
  hasNotifications: boolean = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private notificationService: NotificationService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.updateTitle();
    this.refreshUnread();
    this.loadCompanyName();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.updateTitle();
        this.refreshUnread();
      });
  }

  private loadCompanyName() {
    const companyId = this.authService.getCompanyId();
    if (!companyId) {
      return;
    }
    this.authService.getCompanyProfile(companyId).subscribe({
      next: profile => this.companyName = profile.legalName || profile.name || 'Поставщик',
      error: () => {}
    });
  }

  private refreshUnread() {
    if (!this.authService.getToken()) {
      this.hasNotifications = false;
      return;
    }
    this.notificationService.getUnreadCount().subscribe({
      next: count => this.hasNotifications = count > 0,
      error: () => this.hasNotifications = false
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('/');
  }

  private updateTitle() {
    if (this.pageTitle) {
      this.resolvedTitle = this.pageTitle;
      return;
    }

    let activeRoute = this.route.root;
    while (activeRoute.firstChild) {
      activeRoute = activeRoute.firstChild;
    }

    this.resolvedTitle = activeRoute.snapshot.data['title'] || 'Главная панель';
  }
}
