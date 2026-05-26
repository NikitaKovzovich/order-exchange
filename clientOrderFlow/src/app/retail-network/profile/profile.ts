import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AddressService } from '../../services/address.service';
import { CompanyProfile, ProfileUpdateRequest } from '../../models/api.models';

interface UiNotification {
  type: 'success' | 'error';
  message: string;
}

@Component({
  selector: 'app-retail-profile',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  profile: CompanyProfile | null = null;
  isLoading: boolean = false;
  isSaving: boolean = false;
  editMode: boolean = false;
  notification: UiNotification | null = null;

  editForm: ProfileUpdateRequest = {};

  showAddAddress: boolean = false;
  newAddress = { addressType: 'delivery', fullAddress: '' };
  isAddingAddress: boolean = false;

  constructor(
    private authService: AuthService,
    private addressService: AddressService
  ) {}

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    const companyId = this.authService.getCompanyId();
    if (!companyId) {
      this.notification = { type: 'error', message: 'Не удалось определить компанию.' };
      return;
    }
    this.isLoading = true;
    this.authService.getCompanyProfile(companyId).subscribe({
      next: profile => {
        this.profile = profile;
        this.isLoading = false;
      },
      error: () => {
        this.notification = { type: 'error', message: 'Не удалось загрузить профиль.' };
        this.isLoading = false;
      }
    });
  }

  startEdit() {
    if (!this.profile) {
      return;
    }
    this.editForm = {
      name: this.profile.name || this.profile.legalName,
      contactPhone: this.profile.contactPhone
    };
    this.editMode = true;
    this.notification = null;
  }

  cancelEdit() {
    this.editMode = false;
    this.editForm = {};
  }

  save() {
    this.isSaving = true;
    this.authService.updateProfile(this.editForm).subscribe({
      next: () => {
        this.isSaving = false;
        this.editMode = false;
        this.notification = { type: 'success', message: 'Данные сохранены.' };
        this.loadProfile();
      },
      error: () => {
        this.isSaving = false;
        this.notification = { type: 'error', message: 'Не удалось сохранить данные.' };
      }
    });
  }

  toggleAddAddress() {
    this.showAddAddress = !this.showAddAddress;
    this.newAddress = { addressType: 'delivery', fullAddress: '' };
  }

  addAddress() {
    if (!this.newAddress.fullAddress.trim()) {
      this.notification = { type: 'error', message: 'Укажите адрес.' };
      return;
    }
    this.isAddingAddress = true;
    this.addressService.addAddress({
      addressType: this.newAddress.addressType,
      fullAddress: this.newAddress.fullAddress.trim(),
      isDefault: false
    }).subscribe({
      next: () => {
        this.isAddingAddress = false;
        this.showAddAddress = false;
        this.notification = { type: 'success', message: 'Адрес добавлен.' };
        this.loadProfile();
      },
      error: () => {
        this.isAddingAddress = false;
        this.notification = { type: 'error', message: 'Не удалось добавить адрес.' };
      }
    });
  }

  getLegalFormLabel(value?: string | null): string {
    switch ((value || '').toUpperCase()) {
      case 'IE': return 'ИП';
      case 'LLC': return 'ООО';
      case 'OJSC': return 'ОАО';
      case 'CJSC': return 'ЗАО';
      case 'PJSC': return 'ПАО';
      case 'PUE': return 'ЧУП';
      default: return value || '—';
    }
  }

  getAddressTypeLabel(value?: string | null): string {
    switch ((value || '').toLowerCase()) {
      case 'legal': return 'Юридический';
      case 'postal': return 'Почтовый';
      case 'shipping': return 'Отгрузки';
      case 'delivery': return 'Доставки';
      default: return value || 'Адрес';
    }
  }

  formatDate(value?: string | null): string {
    return value ? new Date(value).toLocaleDateString('ru-RU') : '—';
  }
}
