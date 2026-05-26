import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { SupplierDirectoryItem } from '../../models/api.models';
import { PartnershipService } from '../../services/partnership.service';

@Component({
  selector: 'app-retail-suppliers',
  imports: [CommonModule, FormsModule],
  templateUrl: './suppliers.html',
  styleUrl: './suppliers.css'
})
export class Suppliers implements OnInit {
  searchQuery: string = '';
  suppliers: SupplierDirectoryItem[] = [];
  filteredSuppliers: SupplierDirectoryItem[] = [];
  isLoading: boolean = false;
  isSubmitting: Record<number, boolean> = {};
  errorMessage: string = '';
  customerCompanyName: string = '';
  customerUnp: string = '';

  showRequestModal: boolean = false;
  modalSupplier: SupplierDirectoryItem | null = null;
  modalError: string = '';
  contractForm = {
    contractNumber: '',
    contractDate: '',
    contractEndDate: ''
  };

  constructor(
    private partnershipService: PartnershipService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCurrentCompanyProfile();
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.partnershipService.getCustomerSuppliers(this.searchQuery.trim() || undefined).subscribe({
      next: suppliers => {
        this.suppliers = suppliers;
        this.filteredSuppliers = [...suppliers];
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading suppliers:', error);
        this.errorMessage = 'Не удалось загрузить базу поставщиков';
        this.isLoading = false;
      }
    });
  }

  applyFilters() {
    this.loadSuppliers();
  }

  resetFilters() {
    this.searchQuery = '';
    this.loadSuppliers();
  }

  openRequestModal(supplier: SupplierDirectoryItem): void {
    this.modalSupplier = supplier;
    this.modalError = '';
    this.contractForm = {
      contractNumber: '',
      contractDate: '',
      contractEndDate: ''
    };
    this.showRequestModal = true;
  }

  closeRequestModal(): void {
    this.showRequestModal = false;
    this.modalSupplier = null;
    this.modalError = '';
  }

  submitPartnershipRequest(): void {
    if (!this.modalSupplier) {
      return;
    }

    if (!this.contractForm.contractNumber.trim()) {
      this.modalError = 'Укажите номер договора.';
      return;
    }
    if (!this.contractForm.contractDate) {
      this.modalError = 'Укажите дату подписания.';
      return;
    }
    if (!this.contractForm.contractEndDate) {
      this.modalError = 'Укажите дату окончания действия.';
      return;
    }
    if (this.contractForm.contractEndDate < this.contractForm.contractDate) {
      this.modalError = 'Дата окончания не может быть раньше даты подписания.';
      return;
    }

    const supplier = this.modalSupplier;
    this.isSubmitting[supplier.companyId] = true;
    this.modalError = '';

    this.partnershipService.createPartnershipRequest({
      supplierId: supplier.companyId,
      contractNumber: this.contractForm.contractNumber.trim(),
      contractDate: this.contractForm.contractDate,
      contractEndDate: this.contractForm.contractEndDate,
      customerCompanyName: this.customerCompanyName || 'Текущая торговая сеть',
      customerUnp: this.customerUnp || '000000000'
    }).subscribe({
      next: response => {
        supplier.partnershipId = response.id;
        supplier.partnershipStatus = response.status;
        this.isSubmitting[supplier.companyId] = false;
        this.closeRequestModal();
      },
      error: error => {
        console.error('Error creating partnership:', error);
        this.isSubmitting[supplier.companyId] = false;
        this.modalError = error.error?.message || 'Не удалось отправить запрос.';
      }
    });
  }

  openSupplierCatalog(supplier: SupplierDirectoryItem): void {
    this.router.navigate(['/retail/suppliers', supplier.companyId, 'catalog']);
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активный партнёр';
      case 'PENDING':
        return 'Запрос отправлен';
      default:
        return 'Доступен для запроса';
    }
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-700';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  private loadCurrentCompanyProfile(): void {
    const companyId = this.authService.getCompanyId();
    if (!companyId) {
      return;
    }

    this.authService.getCompanyProfile(companyId).subscribe({
      next: profile => {
        this.customerCompanyName = profile.legalName || profile.name || '';
        this.customerUnp = profile.taxId || '';
      },
      error: error => {
        console.error('Error loading retail company profile:', error);
      }
    });
  }
}
