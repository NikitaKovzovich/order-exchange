import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../services/catalog.service';
import { Product, ProductStatus } from '../../models/api.models';

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

  products: Product[] = [];
  filteredProducts: Product[] = [];

  constructor(private catalogService: CatalogService) {}

  ngOnInit() {
    this.loadProducts();
  }

  loadProducts() {
    this.isLoading = true;
    this.catalogService.getSupplierProducts({
      page: this.currentPage,
      size: 20,
      search: this.searchQuery || undefined
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

  deleteProduct(product: Product) {
    if (confirm(`Удалить товар "${product.name}"?`)) {
      this.catalogService.deleteProduct(product.id).subscribe({
        next: () => this.loadProducts(),
        error: (error) => console.error('Error deleting product:', error)
      });
    }
  }

  getStatusClass(status: ProductStatus): string {
    switch(status) {
      case 'PUBLISHED':
        return 'text-green-600 bg-green-200';
      case 'DRAFT':
        return 'text-yellow-600 bg-yellow-200';
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
