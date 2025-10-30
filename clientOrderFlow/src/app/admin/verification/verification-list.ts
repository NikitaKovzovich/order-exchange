import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';
import { AdminService, VerificationRequest } from '../../services/admin.service';

interface RequestDisplay extends VerificationRequest {
  roleClass: string;
  statusClass: string;
  displayDate: string;
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
  selectedStatus: string = '';

  requests: RequestDisplay[] = [];
  filteredRequests: RequestDisplay[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadRequests();
  }

  loadRequests() {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getVerificationRequests().subscribe({
      next: (data) => {
        this.requests = data.map(req => this.mapToDisplay(req));
        this.filteredRequests = [...this.requests];
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
    const roleClass = this.getRoleClass(req.companyName);
    const statusClass = this.getStatusClass(req.status);
    const displayDate = this.formatDate(req.submittedAt);

    return {
      ...req,
      roleClass,
      statusClass,
      displayDate
    };
  }

  private getRoleClass(companyName: string): string {
    // Определяем тип по названию компании (временно, пока API не возвращает role)
    // В будущем это должно приходить из API
    return 'bg-green-100 text-green-800'; // По умолчанию поставщик
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
    this.filteredRequests = this.requests.filter(request => {
      let match = true;

      if (this.searchQuery) {
        match = match && (
          request.companyName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          request.taxId.includes(this.searchQuery)
        );
      }

      if (this.selectedStatus) {
        const statusMap: { [key: string]: string } = {
          'Ожидает проверки': 'PENDING',
          'Одобрена': 'APPROVED',
          'Отклонена': 'REJECTED'
        };
        match = match && request.status === statusMap[this.selectedStatus];
      }

      return match;
    });
  }

  getStatusDisplayName(status: string): string {
    const statusMap: { [key: string]: string } = {
      'PENDING': 'Ожидает проверки',
      'APPROVED': 'Одобрена',
      'REJECTED': 'Отклонена'
    };
    return statusMap[status] || status;
  }
}
