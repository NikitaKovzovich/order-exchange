import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { AdminService, VerificationRequest } from '../../services/admin.service';

interface RequestDisplay extends VerificationRequest {
  roleClass: string;
  statusClass: string;
  displayDate: string;
}

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'admin-verification-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './verification-list.html',
  styleUrls: ['./verification-list.css']
})
export class VerificationList implements OnInit {
  searchQuery: string = '';
  selectedRole: string = '';
  selectedStatus: '' | 'PENDING' | 'APPROVED' | 'REJECTED' = '';

  requests: RequestDisplay[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  notification: UiNotification | null = null;
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
    this.loadRequests();
  }

  clearNotification(): void {
    this.notification = null;
  }

  loadRequests() {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.searchVerificationRequests({
      status: this.selectedStatus || undefined,
      role: this.selectedRole || undefined,
      search: this.searchQuery || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (data) => {
        this.requests = data.content.map(req => this.mapToDisplay(req));
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading verification requests:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка при загрузке списка заявок';
      }
    });
  }

  private mapToDisplay(req: VerificationRequest): RequestDisplay {
    const roleClass = this.getRoleClass(req.role);
    const statusClass = this.getStatusClass(req.status);
    const displayDate = this.formatDate(req.submittedAt);

    return {
      ...req,
      roleClass,
      statusClass,
      displayDate
    };
  }

  private getRoleClass(role?: string): string {
    if (role === 'RETAIL_CHAIN') {
      return 'bg-blue-100 text-blue-800';
    }

    return 'bg-green-100 text-green-800';
  }

  private getStatusClass(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'bg-yellow-100 text-yellow-800',
      'APPROVED': 'bg-green-100 text-green-800',
      'REJECTED': 'bg-red-100 text-red-800'
    };
    return statusMap[status] || 'bg-gray-100 text-gray-800';
  }

  private formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadRequests();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedRole = '';
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadRequests();
  }

  goToPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }

    this.currentPage -= 1;
    this.loadRequests();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.currentPage += 1;
    this.loadRequests();
  }

  getStatusDisplayName(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'Ожидает проверки',
      'APPROVED': 'Одобрена',
      'REJECTED': 'Отклонена'
    };
    return statusMap[status] || status;
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
    const notification = state?.['verificationNotification'] as UiNotification | undefined;

    if (!notification?.message) {
      return;
    }

    this.notification = notification;

    if (historyState?.verificationNotification) {
      const { verificationNotification, ...restState } = historyState;
      window.history.replaceState(restState, '', this.router.url);
    }
  }
}
