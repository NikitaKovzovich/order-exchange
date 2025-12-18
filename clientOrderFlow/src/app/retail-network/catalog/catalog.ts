import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CatalogService } from '../../services/catalog.service';
import { CartService } from '../../services/cart.service';
import { Product, Category } from '../../models/api.models';

@Component({
  selector: 'app-retail-catalog',
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css'
})
export class Catalog implements OnInit {
  searchQuery: string = '';
  selectedSupplierId: number | null = null;
  selectedCategoryId: number | null = null;
  minPrice: number | null = null;
  maxPrice: number | null = null;

  products: Product[] = [];
  categories: Category[] = [];
  isLoading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;

  constructor(
    private catalogService: CatalogService,
    private cartService: CartService
  ) {}

  ngOnInit() {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories() {
    this.catalogService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => console.error('Error loading categories:', error)
    });
  }

  loadProducts() {
    this.isLoading = true;
    this.catalogService.searchProducts({
      categoryId: this.selectedCategoryId || undefined,
      supplierId: this.selectedSupplierId || undefined,
      minPrice: this.minPrice || undefined,
      maxPrice: this.maxPrice || undefined,
      search: this.searchQuery || undefined,
      page: this.currentPage,
      size: 20
    }).subscribe({
      next: (response) => {
        this.products = response.content;
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

  applyFilters() {
    this.currentPage = 0;
    this.loadProducts();
  }

  clearFilters() {
    this.searchQuery = '';
    this.selectedSupplierId = null;
    this.selectedCategoryId = null;
    this.minPrice = null;
    this.maxPrice = null;
    this.currentPage = 0;
    this.loadProducts();
  }

  addToCart(product: Product) {
    this.cartService.addItem({
      productId: product.id,
      supplierId: product.supplierId,
      productName: product.name,
      productSku: product.sku,
      quantity: 1,
      unitPrice: product.pricePerUnit,
      vatRate: product.vatRateValue
    }).subscribe({
      next: () => {
        console.log('Added to cart:', product.name);
      },
      error: (error) => console.error('Error adding to cart:', error)
    });
  }

  onPageChange(page: number) {
    this.currentPage = page;
    this.loadProducts();
  }

  formatPrice(price: number): string {
    return price.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}

