import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Header } from '../../shared/header/header';
import { AdminService, AdminUserDetail, AdminUserUpdatePayload, UserEvent } from '../../../services/admin.service';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'admin-user-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, Header, RouterModule],
  templateUrl: './user-detail.html',
  styleUrls: ['./user-detail.css']
})
export class UserDetail implements OnInit {
  userId: number = 0;
  user: AdminUserDetail | null = null;
  userEvents: UserEvent[] = [];
  isLoading: boolean = false;
  isProcessing: boolean = false;
  errorMessage: string = '';
  notification: UiNotification | null = null;
  showEditModal = false;
  showBlockModal = false;
  showDeleteModal = false;
  editForm: AdminUserUpdatePayload = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService
  ) {}

  ngOnInit() {
    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadUser();
  }

  clearNotification(): void {
    this.notification = null;
  }

  loadUser(): void {
    if (!this.userId) {
      this.errorMessage = 'Некорректный ID пользователя';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getUserById(this.userId).subscribe({
      next: user => {
        this.user = user;
        this.resetEditForm(user);
        this.isLoading = false;
        this.loadUserEvents();
      },
      error: error => {
        console.error('Error loading user detail:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить профиль пользователя';
        this.isLoading = false;
      }
    });
  }

  openEditModal(): void {
    if (!this.user) {
      return;
    }

    this.resetEditForm(this.user);
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
  }

  saveUser(): void {
    if (!this.user) {
      return;
    }

    this.isProcessing = true;
    this.adminService.updateUser(this.user.id, this.editForm).subscribe({
      next: response => {
        this.notification = {
          type: 'success',
          message: response.message || 'Данные пользователя обновлены.'
        };
        this.showEditModal = false;
        this.isProcessing = false;
        this.loadUser();
      },
      error: error => {
        console.error('Error updating user:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось обновить данные пользователя.'
        };
        this.isProcessing = false;
      }
    });
  }

  blockUser() {
    if (!this.user) {
      return;
    }

    this.isProcessing = true;
    this.adminService.blockUser(this.user.id).subscribe({
      next: response => {
        this.showBlockModal = false;
        this.navigateToUsers(response.message || 'Пользователь заблокирован.');
      },
      error: error => {
        console.error('Error blocking user:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось заблокировать пользователя.'
        };
        this.isProcessing = false;
      }
    });
  }

  unblockUser(): void {
    if (!this.user) {
      return;
    }

    this.isProcessing = true;
    this.adminService.unblockUser(this.user.id).subscribe({
      next: response => {
        this.navigateToUsers(response.message || 'Пользователь разблокирован.');
      },
      error: error => {
        console.error('Error unblocking user:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось разблокировать пользователя.'
        };
        this.isProcessing = false;
      }
    });
  }

  deleteUser() {
    if (!this.user) {
      return;
    }

    this.isProcessing = true;
    this.adminService.deleteUser(this.user.id).subscribe({
      next: response => {
        this.showDeleteModal = false;
        this.navigateToUsers(response.message || 'Пользователь удалён.');
      },
      error: error => {
        console.error('Error deleting user:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось удалить пользователя.'
        };
        this.isProcessing = false;
      }
    });
  }

  get roleLabel(): string {
    if (!this.user) {
      return '—';
    }

    return this.user.role === 'SUPPLIER' ? 'Поставщик' : this.user.role === 'RETAIL_CHAIN' ? 'Торговая сеть' : this.user.role;
  }

  get roleClass(): string {
    if (this.user?.role === 'RETAIL_CHAIN') {
      return 'bg-blue-100 text-blue-800';
    }

    if (this.user?.role === 'SUPPLIER') {
      return 'bg-green-100 text-green-800';
    }

    return 'bg-gray-100 text-gray-800';
  }

  get statusLabel(): string {
    switch (this.user?.status) {
      case 'ACTIVE':
        return 'Активен';
      case 'BLOCKED':
        return 'Заблокирован';
      case 'DELETED':
        return 'Удалён';
      default:
        return this.user?.status || '—';
    }
  }

  get statusClass(): string {
    switch (this.user?.status) {
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

  get companyName(): string {
    return this.user?.companyProfile?.name || this.user?.companyProfile?.legalName || this.user?.company?.legalName || '—';
  }

  get companyFullName(): string {
    return this.user?.companyProfile?.legalName || this.user?.company?.legalName || '—';
  }

  get contactPhone(): string {
    return this.user?.companyProfile?.contactPhone || '—';
  }

  get address(): string {
    return this.user?.companyProfile?.addresses?.[0]?.fullAddress || '—';
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return '—';
    }

    return new Date(value).toLocaleDateString('ru-RU');
  }

  formatDateTime(value?: string | null): string {
    if (!value) {
      return '—';
    }

    return new Date(value).toLocaleString('ru-RU');
  }

  formatAmount(value?: number | null): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private loadUserEvents(): void {
    this.adminService.getUserEvents(this.userId, 0, 10).subscribe({
      next: response => {
        this.userEvents = response.content;
      },
      error: error => {
        console.error('Error loading user events:', error);
      }
    });
  }

  private navigateToUsers(message: string): void {
    this.router.navigate(['/admin/users'], {
      state: {
        adminUsersNotification: {
          type: 'success',
          message
        }
      }
    });
  }

  private resetEditForm(user: AdminUserDetail): void {
    this.editForm = {
      email: user.email,
      name: user.companyProfile?.name || user.companyProfile?.legalName || user.company?.legalName || '',
      contactPhone: user.companyProfile?.contactPhone || '',
      bankName: user.companyProfile?.bankName || '',
      bic: user.companyProfile?.bic || '',
      accountNumber: user.companyProfile?.accountNumber || '',
      directorName: user.companyProfile?.directorName || '',
      chiefAccountantName: user.companyProfile?.chiefAccountantName || '',
      paymentTerms: user.companyProfile?.paymentTerms || ''
    };
  }
}
