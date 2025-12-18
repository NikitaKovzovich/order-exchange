import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';
import { CatalogService } from '../../services/catalog.service';
import { Unit, VatRate, Category } from '../../models/api.models';

@Component({
  selector: 'admin-dictionaries',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './dictionaries.html',
  styleUrls: ['./dictionaries.css']
})
export class Dictionaries implements OnInit {
  activeTab: string = 'units';
  isLoading: boolean = false;

  units: Unit[] = [];
  vatRates: VatRate[] = [];
  categories: Category[] = [];

  showEditModal: boolean = false;
  showDeleteModal: boolean = false;
  editingItem: any = null;
  selectedItem: any = null;
  editValue: string = '';
  editParentId: number | null = null;

  constructor(private catalogService: CatalogService) {}

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading = true;

    this.catalogService.getUnits().subscribe({
      next: (units) => this.units = units,
      error: (error) => console.error('Error loading units:', error)
    });

    this.catalogService.getVatRates().subscribe({
      next: (vatRates) => this.vatRates = vatRates,
      error: (error) => console.error('Error loading VAT rates:', error)
    });

    this.catalogService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
        this.isLoading = false;
      }
    });
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  openEditModal(item?: any) {
    if (item) {
      this.editingItem = item;
      this.editValue = item.name || item.description || '';
      this.editParentId = item.parentId || null;
    } else {
      this.editingItem = null;
      this.editValue = '';
      this.editParentId = null;
    }
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
    this.editingItem = null;
    this.editValue = '';
    this.editParentId = null;
  }

  saveItem() {
    if (!this.editValue.trim()) return;

    if (this.activeTab === 'categories') {
      if (this.editingItem) {
        this.catalogService.updateCategory(this.editingItem.id, {
          name: this.editValue,
          parentId: this.editParentId
        }).subscribe({
          next: () => {
            this.loadData();
            this.closeEditModal();
          },
          error: (error) => console.error('Error updating category:', error)
        });
      } else {
        this.catalogService.createCategory({
          name: this.editValue,
          parentId: this.editParentId
        }).subscribe({
          next: () => {
            this.loadData();
            this.closeEditModal();
          },
          error: (error) => console.error('Error creating category:', error)
        });
      }
    } else {
      this.closeEditModal();
    }
  }

  openDeleteModal(item: any) {
    this.selectedItem = item;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.selectedItem = null;
  }

  confirmDelete() {
    if (!this.selectedItem) return;

    if (this.activeTab === 'categories') {
      this.catalogService.deleteCategory(this.selectedItem.id).subscribe({
        next: () => {
          this.loadData();
          this.closeDeleteModal();
        },
        error: (error) => {
          console.error('Error deleting category:', error);
          alert('Невозможно удалить категорию с товарами');
          this.closeDeleteModal();
        }
      });
    } else {
      this.closeDeleteModal();
    }
  }
}
