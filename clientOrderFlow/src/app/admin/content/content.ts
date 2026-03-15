import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';
import { AdminService, User } from '../../services/admin.service';
import { CatalogService } from '../../services/catalog.service';
import { Category as ApiCategory, Product as ApiProduct } from '../../models/api.models';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

interface ProductView {
  id: number;
  supplierId: number;
  categoryId?: number;
  name: string;
  supplier: string;
  category: string;
  price: string;
  status: string;
  statusClass: string;
}

interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  parentName?: string | null;
  productCount: number;
}

@Component({
  selector: 'admin-content',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './content.html',
  styleUrls: ['./content.css']
})
export class Content implements OnInit {
  activeTab: string = 'products';

  searchQuery: string = '';
  selectedCategoryId: string = '';
  selectedSupplierId: string = '';
  isLoading: boolean = false;
  errorMessage: string = '';
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  readonly pageSize: number = 20;

  products: ProductView[] = [];
  suppliers: User[] = [];

  categories: Category[] = [];

  showDeleteModal: boolean = false;
  showHideModal: boolean = false;
  showNewCategoryModal: boolean = false;
  selectedItem: any = null;
  newCategoryName: string = '';
  editingCategory: Category | null = null;
  notification: UiNotification | null = null;

  constructor(
    private adminService: AdminService,
    private catalogService: CatalogService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadCategories();
    this.loadSuppliers();
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;

    if (tab === 'products') {
      this.loadProducts();
    }
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedCategoryId = '';
    this.selectedSupplierId = '';
    this.currentPage = 0;
    this.loadProducts();
  }

  openHideModal(product: ProductView) {
    this.selectedItem = product;
    this.showHideModal = true;
  }

  closeHideModal() {
    this.showHideModal = false;
    this.selectedItem = null;
  }

  confirmHide() {
    if (!this.selectedItem) return;

    this.adminService.hideProduct(this.selectedItem.id).subscribe({
      next: () => {
        this.loadProducts();
        this.closeHideModal();
      },
      error: error => console.error('Error hiding product:', error)
    });
  }

  showProduct(product: ProductView) {
    this.adminService.showProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: error => console.error('Error showing product:', error)
    });
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
    if (this.selectedItem) {
      if (this.activeTab === 'products') {
        this.adminService.deleteAdminProduct(this.selectedItem.id).subscribe({
          next: () => {
            this.loadProducts();
            this.closeDeleteModal();
          },
          error: error => console.error('Error deleting product:', error)
        });
        return;
      } else if (this.activeTab === 'categories') {
        const deletedCategoryId = this.selectedItem.id;
        this.catalogService.deleteCategory(deletedCategoryId).subscribe({
          next: () => {
            if (this.selectedCategoryId === String(deletedCategoryId)) {
              this.selectedCategoryId = '';
              this.currentPage = 0;
              this.loadProducts();
            }

            this.notification = {
              type: 'success',
              message: 'Категория удалена.'
            };
            this.loadCategories();
            this.closeDeleteModal();
          },
          error: error => {
            console.error('Error deleting category:', error);
            this.notification = {
              type: 'error',
              message: error.error?.message || 'Не удалось удалить категорию.'
            };
            this.closeDeleteModal();
          }
        });
        return;
      }
    }
    this.closeDeleteModal();
  }

  openNewCategoryModal() {
    this.editingCategory = null;
    this.showNewCategoryModal = true;
    this.newCategoryName = '';
  }

  closeNewCategoryModal() {
    this.showNewCategoryModal = false;
    this.newCategoryName = '';
    this.editingCategory = null;
  }

  addCategory() {
    const categoryName = this.newCategoryName.trim();
    if (!categoryName) {
      return;
    }

    const request = {
      name: categoryName,
      parentId: this.editingCategory?.parentId ?? null
    };

    const action = this.editingCategory
      ? this.catalogService.updateCategory(this.editingCategory.id, request)
      : this.catalogService.createCategory(request);

    action.subscribe({
      next: () => {
        this.notification = {
          type: 'success',
          message: this.editingCategory ? 'Категория обновлена.' : 'Категория создана.'
        };
        this.loadCategories();
        this.closeNewCategoryModal();
      },
      error: error => {
        console.error('Error saving category:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось сохранить категорию.'
        };
      }
    });
  }

  editCategory(category: Category) {
    this.editingCategory = category;
    this.newCategoryName = category.name;
    this.showNewCategoryModal = true;
  }

  goToPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }

    this.currentPage -= 1;
    this.loadProducts();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.currentPage += 1;
    this.loadProducts();
  }

  private loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getAdminProducts(
      this.selectedSupplierId ? Number(this.selectedSupplierId) : undefined,
      this.selectedCategoryId ? Number(this.selectedCategoryId) : undefined,
      undefined,
      this.searchQuery || undefined,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: page => {
        this.products = page.content.map(product => this.mapProduct(product));
        this.currentPage = page.number;
        this.totalPages = page.totalPages;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading admin products:', error);
        this.errorMessage = 'Не удалось загрузить товары для модерации';
        this.isLoading = false;
      }
    });
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: (categories: ApiCategory[]) => {
        this.categories = categories.map(category => ({
          id: category.id,
          name: category.name,
          parentId: category.parentId,
          parentName: category.parentName,
          productCount: category.productCount
        }));
      },
      error: error => console.error('Error loading categories:', error)
    });
  }

  private loadSuppliers(): void {
    this.adminService.searchUsers({ role: 'SUPPLIER', page: 0, size: 1000 }).subscribe({
      next: response => {
        this.suppliers = response.content;
      },
      error: error => console.error('Error loading suppliers for admin content filters:', error)
    });
  }

  private mapProduct(product: ApiProduct): ProductView {
    const isArchived = product.status === 'ARCHIVED';

    return {
      id: product.id,
      supplierId: product.supplierId,
      categoryId: product.category?.id,
      name: product.name,
      supplier: product.supplierName || `Поставщик #${product.supplierId}`,
      category: product.category?.name || 'Без категории',
      price: `${Number(product.pricePerUnit || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} BYN`,
      status: isArchived ? 'Скрыт' : 'Активен',
      statusClass: isArchived ? 'bg-gray-200 text-gray-800' : 'bg-green-100 text-green-800'
    };
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
}
