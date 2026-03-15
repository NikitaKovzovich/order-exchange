import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Partnership } from '../../models/api.models';
import { PartnershipService } from '../../services/partnership.service';

@Component({
  selector: 'app-supplier-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clients.html',
  styleUrl: './clients.css'
})
export class Clients implements OnInit {
  activeTab: 'pending' | 'active' | 'all' = 'pending';
  pendingPartnerships: Partnership[] = [];
  activePartnerships: Partnership[] = [];
  allPartnerships: Partnership[] = [];
  editingId: number | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';

  contractForm = {
    contractNumber: '',
    contractDate: '',
    contractEndDate: ''
  };

  constructor(private partnershipService: PartnershipService) {}

  ngOnInit(): void {
    this.loadPartnerships();
  }

  setTab(tab: 'pending' | 'active' | 'all'): void {
    this.activeTab = tab;
  }

  accept(partnership: Partnership): void {
    this.partnershipService.acceptPartnership(partnership.id).subscribe({
      next: () => this.loadPartnerships(),
      error: error => console.error('Error accepting partnership:', error)
    });
  }

  reject(partnership: Partnership): void {
    this.partnershipService.rejectPartnership(partnership.id).subscribe({
      next: () => this.loadPartnerships(),
      error: error => console.error('Error rejecting partnership:', error)
    });
  }

  startEdit(partnership: Partnership): void {
    this.editingId = partnership.id;
    this.contractForm = {
      contractNumber: partnership.contractNumber || '',
      contractDate: partnership.contractDate || '',
      contractEndDate: partnership.contractEndDate || ''
    };
  }

  cancelEdit(): void {
    this.editingId = null;
    this.contractForm = {
      contractNumber: '',
      contractDate: '',
      contractEndDate: ''
    };
  }

  saveContract(partnership: Partnership): void {
    if (!this.contractForm.contractNumber || !this.contractForm.contractDate || !this.contractForm.contractEndDate) {
      return;
    }

    this.partnershipService.updateContract(partnership.id, {
      contractNumber: this.contractForm.contractNumber,
      contractDate: this.contractForm.contractDate,
      contractEndDate: this.contractForm.contractEndDate
    }).subscribe({
      next: () => {
        this.cancelEdit();
        this.loadPartnerships();
      },
      error: error => console.error('Error updating contract:', error)
    });
  }

  get visiblePartnerships(): Partnership[] {
    switch (this.activeTab) {
      case 'active':
        return this.activePartnerships;
      case 'all':
        return this.allPartnerships;
      default:
        return this.pendingPartnerships;
    }
  }

  formatDate(value?: string | null): string {
    return value ? new Date(value).toLocaleDateString('ru-RU') : '—';
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активный договор';
      case 'PENDING':
        return 'Новая заявка';
      case 'REJECTED':
        return 'Отклонено';
      default:
        return status;
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-700';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-700';
      case 'REJECTED':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  private loadPartnerships(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.partnershipService.getSupplierPendingPartnerships().subscribe({
      next: pending => {
        this.pendingPartnerships = pending;

        this.partnershipService.getSupplierActivePartnerships().subscribe({
          next: active => {
            this.activePartnerships = active;

            this.partnershipService.getSupplierPartnerships().subscribe({
              next: all => {
                this.allPartnerships = all;
                this.isLoading = false;
              },
              error: error => this.handleLoadError(error)
            });
          },
          error: error => this.handleLoadError(error)
        });
      },
      error: error => this.handleLoadError(error)
    });
  }

  private handleLoadError(error: unknown): void {
    console.error('Error loading partnerships:', error);
    this.errorMessage = 'Не удалось загрузить партнёрства';
    this.isLoading = false;
  }
}

