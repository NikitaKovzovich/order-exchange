import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';
import { AdminService, User } from '../../services/admin.service';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'admin-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Header],
  templateUrl: './users.html',
  styleUrls: ['./users.css']
})
export class Users implements OnInit {
  activeTab: 'providers' | 'retailers' = 'providers';
  searchTerm: string = '';
  selectedStatus: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  notification: UiNotification | null = null;

  users: User[] = [];
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  readonly pageSize: number = 20;

  constructor(
    private adminService: AdminService,
    private router: Router
  ) {}

  ngOnInit() {
    this.consumeFlashNotification();
    this.loadUsers();
  }

  clearNotification(): void {
    this.notification = null;
  }

  loadUsers() {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.searchUsers({
      role: this.activeTab === 'providers' ? 'SUPPLIER' : 'RETAIL_CHAIN',
      status: this.selectedStatus || undefined,
      search: this.searchTerm || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        this.users = response.content;
        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading users:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить список пользователей';
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: 'providers' | 'retailers') {
    if (this.activeTab === tab) {
      return;
    }

    this.activeTab = tab;
    this.currentPage = 0;
    this.loadUsers();
  }

  applyFilters(): void {
    this.currentPage = 0;
    this.loadUsers();
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadUsers();
  }

  goToPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }

    this.currentPage -= 1;
    this.loadUsers();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.currentPage += 1;
    this.loadUsers();
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активен';
      case 'BLOCKED':
        return 'Заблокирован';
      case 'DELETED':
        return 'Удалён';
      default:
        return status || '—';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'BLOCKED':
        return 'bg-red-100 text-red-800';
      case 'DELETED':
        return 'bg-gray-200 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatDate(dateStr: string): string {
    if (!dateStr) {
      return '—';
    }

    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  get rangeStart(): number {
    if (this.totalElements === 0) {
      return 0;
    }

    return this.currentPage * this.pageSize + 1;
  }

  get rangeEnd(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
  }

  private consumeFlashNotification(): void {
    const navigationState = this.router.getCurrentNavigation()?.extras.state;
    const historyState = window.history.state;
    const state = navigationState || historyState;
    const notification = state?.['adminUsersNotification'] as UiNotification | undefined;

    if (!notification?.message) {
      return;
    }

    this.notification = notification;

    if (historyState?.adminUsersNotification) {
      const { adminUsersNotification, ...restState } = historyState;
      window.history.replaceState(restState, '', this.router.url);
    }
  }
}
