import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { AdminService, VerificationDetails } from '../../services/admin.service';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'admin-verification',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './verification.html',
  styleUrls: ['./verification.css']
})
export class Verification implements OnInit {
  requestId: string = '';
  request: VerificationDetails | null = null;
  showApproveModal: boolean = false;
  showRejectModal: boolean = false;
  rejectionReason: string = '';
  rejectValidationMessage: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  notification: UiNotification | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminService: AdminService
  ) {}

  ngOnInit() {
    this.requestId = this.route.snapshot.paramMap.get('id') || '';
    this.loadRequest();
  }

  clearNotification(): void {
    this.notification = null;
  }

  loadRequest() {
    const id = parseInt(this.requestId);
    if (!id) {
      this.errorMessage = 'Некорректный ID заявки';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getVerificationById(id).subscribe({
      next: (data) => {
        this.request = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading verification request:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка при загрузке заявки';
      }
    });
  }

  approve() {
    this.showApproveModal = true;
  }

  closeApproveModal(): void {
    this.showApproveModal = false;
  }

  confirmApprove(): void {
    const id = parseInt(this.requestId);
    if (!id) {
      this.notification = {
        type: 'error',
        message: 'Некорректный ID заявки.'
      };
      this.closeApproveModal();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';
    this.clearNotification();
    this.closeApproveModal();

    this.adminService.approveVerification(id).subscribe({
      next: (response) => {
        console.log('Verification approved:', response);
        this.router.navigate(['/admin/verification'], {
          state: {
            verificationNotification: {
              type: 'success',
              message: response.message || 'Заявка успешно одобрена.'
            }
          }
        });
      },
      error: (error) => {
        console.error('Error approving verification:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка при одобрении заявки';
        this.notification = {
          type: 'error',
          message: this.errorMessage
        };
      }
    });
  }

  openRejectModal() {
    this.rejectValidationMessage = '';
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectionReason = '';
    this.rejectValidationMessage = '';
  }

  confirmRejection() {
    if (!this.rejectionReason.trim()) {
      this.rejectValidationMessage = 'Пожалуйста, укажите причину отклонения.';
      return;
    }

    const id = parseInt(this.requestId);
    this.isLoading = true;
    this.errorMessage = '';
    this.clearNotification();
    this.rejectValidationMessage = '';

    this.adminService.rejectVerification(id, this.rejectionReason).subscribe({
      next: (response) => {
        console.log('Verification rejected:', response);
        this.closeRejectModal();
        this.router.navigate(['/admin/verification'], {
          state: {
            verificationNotification: {
              type: 'success',
              message: response.message || 'Заявка отклонена. Компания получит уведомление.'
            }
          }
        });
      },
      error: (error) => {
        console.error('Error rejecting verification:', error);
        this.isLoading = false;
        this.errorMessage = error.error?.message || 'Ошибка при отклонении заявки';
        this.notification = {
          type: 'error',
          message: this.errorMessage
        };
      }
    });
  }

  getDocumentIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'pdf': 'M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z',
      'image': 'M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l-1-1a2 2 0 00-2.828 0L4 16',
      'signature': 'M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z'
    };
    return icons[type] || icons['pdf'];
  }
}
