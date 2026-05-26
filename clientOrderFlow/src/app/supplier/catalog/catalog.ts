import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../services/catalog.service';
import { Category, Product, ProductStatus } from '../../models/api.models';

@Component({
  selector: 'app-catalog',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css'
})
export class Catalog implements OnInit {
  viewMode: 'grid' | 'list' = 'grid';
  searchQuery: string = '';
  showDraftsAlert: boolean = true;
  isLoading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;

  statusFilter: ProductStatus | '' = '';
  categoryFilter: number | '' = '';
  sortOption: string = 'name:asc';
  categories: Category[] = [];

  products: Product[] = [];
  filteredProducts: Product[] = [];
  productToDelete: Product | null = null;
  private searchDebounceHandle: ReturnType<typeof setTimeout> | null = null;

  constructor(private catalogService: CatalogService) {}

  ngOnInit() {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories() {
    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (error) => console.error('Error loading categories:', error)
    });
  }

  loadProducts() {
    this.isLoading = true;
    const [sortBy, sortDir] = this.sortOption.split(':') as [string, 'asc' | 'desc'];
    this.catalogService.getSupplierProducts({
      page: this.currentPage,
      size: 20,
      search: this.searchQuery || undefined,
      status: this.statusFilter || undefined,
      categoryId: this.categoryFilter || undefined,
      sortBy,
      sortDir
    }).subscribe({
      next: (response) => {
        this.products = response.content;
        this.filteredProducts = [...this.products];
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading products:', error);
        this.isLoading = false;
      }
    });
  }

  get hasDrafts(): boolean {
    return this.products.some(p => p.status === 'DRAFT');
  }

  onSearch() {
    if (this.searchDebounceHandle) {
      clearTimeout(this.searchDebounceHandle);
    }

    this.searchDebounceHandle = setTimeout(() => {
      this.currentPage = 0;
      this.loadProducts();
    }, 300);
  }

  onFilterChange() {
    this.currentPage = 0;
    this.loadProducts();
  }

  publishCatalog() {
    const drafts = this.products.filter(p => p.status === 'DRAFT');
    let published = 0;

    drafts.forEach(product => {
      this.catalogService.publishProduct(product.id).subscribe({
        next: () => {
          published++;
          if (published === drafts.length) {
            this.loadProducts();
            this.showDraftsAlert = false;
          }
        },
        error: (error) => console.error('Error publishing product:', error)
      });
    });
  }

  publishProduct(product: Product) {
    this.catalogService.publishProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (error) => console.error('Error publishing product:', error)
    });
  }

  archiveProduct(product: Product) {
    this.catalogService.archiveProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (error) => console.error('Error archiving product:', error)
    });
  }

  hideProduct(product: Product) {
    this.catalogService.hideProduct(product.id).subscribe({
      next: () => this.loadProducts(),
      error: (error) => console.error('Error hiding product:', error)
    });
  }

  askDeleteProduct(product: Product) {
    this.productToDelete = product;
  }

  cancelDelete() {
    this.productToDelete = null;
  }

  confirmDelete() {
    if (!this.productToDelete) {
      return;
    }
    this.catalogService.deleteProduct(this.productToDelete.id).subscribe({
      next: () => {
        this.productToDelete = null;
        this.loadProducts();
      },
      error: (error) => {
        console.error('Error deleting product:', error);
        this.productToDelete = null;
      }
    });
  }

  getStatusClass(status: ProductStatus): string {
    switch(status) {
      case 'PUBLISHED':
        return 'text-green-600 bg-green-200';
      case 'DRAFT':
        return 'text-yellow-600 bg-yellow-200';
      case 'HIDDEN':
        return 'text-gray-700 bg-gray-300';
      case 'ARCHIVED':
        return 'text-gray-600 bg-gray-200';
      default:
        return 'text-gray-600 bg-gray-200';
    }
  }

  getStatusLabel(status: ProductStatus): string {
    switch(status) {
      case 'PUBLISHED':
        return 'Опубликован';
      case 'DRAFT':
        return 'Черновик';
      case 'HIDDEN':
        return 'Скрыт';
      case 'ARCHIVED':
        return 'В архиве';
      default:
        return status;
    }
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }
}
