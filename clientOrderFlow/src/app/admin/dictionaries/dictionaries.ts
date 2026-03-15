import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Header } from '../shared/header/header';
import { CatalogService } from '../../services/catalog.service';
import { Unit, VatRate } from '../../models/api.models';

type DictionaryTab = 'units' | 'vat';
type NotificationType = 'success' | 'error' | 'info' | 'warning';

@Component({
  selector: 'admin-dictionaries',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './dictionaries.html',
  styleUrls: ['./dictionaries.css']
})
export class Dictionaries implements OnInit {
  activeTab: DictionaryTab = 'units';
  isLoading: boolean = false;
  isSaving: boolean = false;
  isDeleting: boolean = false;
  notification: { type: NotificationType; message: string } | null = null;

  units: Unit[] = [];
  vatRates: VatRate[] = [];

  showEditModal: boolean = false;
  showDeleteModal: boolean = false;
  editingItem: Unit | VatRate | null = null;
  selectedItem: Unit | VatRate | null = null;

  unitName: string = '';
  vatDescription: string = '';
  vatRatePercentage: number | null = null;

  constructor(private catalogService: CatalogService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading = true;

    forkJoin({
      units: this.catalogService.getUnits(),
      vatRates: this.catalogService.getVatRates()
    }).subscribe({
      next: ({ units, vatRates }) => {
        this.units = units;
        this.vatRates = vatRates;
        this.isLoading = false;
      },
      error: (error: unknown) => {
        console.error('Error loading dictionaries:', error);
        this.showNotification('error', 'Не удалось загрузить системные справочники.');
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: DictionaryTab): void {
    this.activeTab = tab;
    this.notification = null;
  }

  openEditModal(item?: Unit | VatRate): void {
    this.editingItem = item ?? null;

    if (this.activeTab === 'units') {
      this.unitName = item && 'name' in item ? item.name : '';
      this.vatDescription = '';
      this.vatRatePercentage = null;
    } else {
      this.unitName = '';
      this.vatDescription = item && 'description' in item ? item.description : '';
      this.vatRatePercentage = item && 'ratePercentage' in item ? item.ratePercentage : null;
    }

    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingItem = null;
    this.isSaving = false;
    this.unitName = '';
    this.vatDescription = '';
    this.vatRatePercentage = null;
  }

  saveItem(): void {
    if (this.isSaving) {
      return;
    }

    if (this.activeTab === 'units') {
      this.saveUnit();
      return;
    }

    this.saveVatRate();
  }

  openDeleteModal(item: Unit | VatRate): void {
    this.selectedItem = item;
    this.showDeleteModal = true;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.selectedItem = null;
    this.isDeleting = false;
  }

  confirmDelete(): void {
    const selectedItem = this.selectedItem;
    if (!selectedItem || this.isDeleting) {
      return;
    }

    this.isDeleting = true;

    const request = this.activeTab === 'units'
      ? this.catalogService.deleteUnit(selectedItem.id)
      : this.catalogService.deleteVatRate(selectedItem.id);

    request.subscribe({
      next: () => {
        this.showNotification(
          'success',
          this.activeTab === 'units' ? 'Единица измерения удалена.' : 'Ставка НДС удалена.'
        );
        this.loadData();
        this.closeDeleteModal();
      },
      error: (error: unknown) => {
        console.error('Error deleting dictionary item:', error);
        this.showNotification(
          'error',
          this.extractErrorMessage(
            error,
            this.activeTab === 'units'
              ? 'Не удалось удалить единицу измерения.'
              : 'Не удалось удалить ставку НДС.'
          )
        );
        this.closeDeleteModal();
      }
    });
  }

  getDeleteItemLabel(): string {
    const selectedItem = this.selectedItem;
    if (!selectedItem) {
      return 'это значение';
    }

    if (this.activeTab === 'units' && 'name' in selectedItem) {
      return selectedItem.name;
    }

    if ('description' in selectedItem) {
      return `${selectedItem.description} (${selectedItem.ratePercentage}%)`;
    }

    return 'это значение';
  }

  private saveUnit(): void {
    const name = this.unitName.trim();
    if (!name) {
      this.showNotification('warning', 'Укажите название единицы измерения.');
      return;
    }

    this.isSaving = true;
    const editingId = this.editingItem?.id;
    const request = editingId !== undefined
      ? this.catalogService.updateUnit(editingId, { name })
      : this.catalogService.createUnit({ name });

    request.subscribe({
      next: () => {
        this.showNotification(
          'success',
          editingId !== undefined ? 'Единица измерения обновлена.' : 'Единица измерения создана.'
        );
        this.loadData();
        this.closeEditModal();
      },
      error: (error: unknown) => {
        console.error('Error saving unit:', error);
        this.isSaving = false;
        this.showNotification('error', this.extractErrorMessage(error, 'Не удалось сохранить единицу измерения.'));
      }
    });
  }

  private saveVatRate(): void {
    const description = this.vatDescription.trim();
    const ratePercentage = Number(this.vatRatePercentage);

    if (!description) {
      this.showNotification('warning', 'Укажите описание ставки НДС.');
      return;
    }

    if (!Number.isFinite(ratePercentage) || ratePercentage < 0) {
      this.showNotification('warning', 'Укажите корректную ставку НДС.');
      return;
    }

    this.isSaving = true;
    const editingId = this.editingItem?.id;
    const request = editingId !== undefined
      ? this.catalogService.updateVatRate(editingId, { description, ratePercentage })
      : this.catalogService.createVatRate({ description, ratePercentage });

    request.subscribe({
      next: () => {
        this.showNotification(
          'success',
          editingId !== undefined ? 'Ставка НДС обновлена.' : 'Ставка НДС создана.'
        );
        this.loadData();
        this.closeEditModal();
      },
      error: (error: unknown) => {
        console.error('Error saving VAT rate:', error);
        this.isSaving = false;
        this.showNotification('error', this.extractErrorMessage(error, 'Не удалось сохранить ставку НДС.'));
      }
    });
  }

  private showNotification(type: NotificationType, message: string): void {
    this.notification = { type, message };
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    if (error instanceof HttpErrorResponse) {
      const payload = error.error as { message?: string; error?: string } | string | null;

      if (typeof payload === 'string' && payload.trim()) {
        return payload;
      }

      if (payload && typeof payload === 'object') {
        if (typeof payload.message === 'string' && payload.message.trim()) {
          return payload.message;
        }

        if (typeof payload.error === 'string' && payload.error.trim()) {
          return payload.error;
        }
      }
    }

    return fallback;
  }
}
