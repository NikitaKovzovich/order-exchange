import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CatalogService } from '../../services/catalog.service';
import { CartService } from '../../services/cart.service';
import { PartnershipService } from '../../services/partnership.service';
import { Category, Product } from '../../models/api.models';

@Component({
  selector: 'app-supplier-catalog',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './supplier-catalog.html',
  styleUrl: './supplier-catalog.css'
})
export class SupplierCatalog implements OnInit {
  supplierId: number = 0;
  supplierName: string = '';

  searchQuery: string = '';
  selectedCategoryId: string = '';
  minPrice: string = '';
  maxPrice: string = '';
  isLoading: boolean = false;
  isCartLoading: boolean = false;
  errorMessage: string = '';
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  readonly pageSize: number = 20;

  products: Product[] = [];
  categories: Category[] = [];
  cartQuantities: Record<number, number> = {};
  cartItemCount: number = 0;
  cartTotalAmount: number = 0;
  private filtersDebounceHandle: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private route: ActivatedRoute,
    private catalogService: CatalogService,
    private cartService: CartService,
    private partnershipService: PartnershipService
  ) {}

  ngOnInit() {
    this.loadCategories();
    this.loadCart();

    this.route.params.subscribe(params => {
      this.supplierId = Number(params['id']);
      this.currentPage = 0;
      this.loadSupplierName();
      this.loadProducts();
    });
  }

  applyFilters() {
    if (this.filtersDebounceHandle) {
      clearTimeout(this.filtersDebounceHandle);
    }

    this.filtersDebounceHandle = setTimeout(() => {
      this.currentPage = 0;
      this.loadProducts();
    }, 300);
  }

  resetFilters() {
    this.searchQuery = '';
    this.selectedCategoryId = '';
    this.minPrice = '';
    this.maxPrice = '';
    this.currentPage = 0;
    this.loadProducts();
  }

  addToCart(product: Product) {
    const currentQuantity = this.getCartQuantity(product.id);
    this.isCartLoading = true;

    const request$ = currentQuantity > 0
      ? this.cartService.updateItem(product.id, { quantity: currentQuantity + 1 })
      : this.cartService.addItem({
          productId: product.id,
          supplierId: product.supplierId,
          productName: product.name,
          productSku: product.sku,
          quantity: 1,
          unitPrice: product.pricePerUnit,
          vatRate: product.vatRateValue
        });

    request$.subscribe({
      next: cart => {
        this.syncCart(cart);
        this.isCartLoading = false;
      },
      error: error => {
        console.error('Error adding product to cart:', error);
        this.isCartLoading = false;
      }
    });
  }

  removeFromCart(product: Product) {
    const currentQuantity = this.getCartQuantity(product.id);
    if (currentQuantity <= 0) {
      return;
    }

    this.isCartLoading = true;
    const request$ = currentQuantity === 1
      ? this.cartService.removeItem(product.id)
      : this.cartService.updateItem(product.id, { quantity: currentQuantity - 1 });

    request$.subscribe({
      next: cart => {
        this.syncCart(cart);
        this.isCartLoading = false;
      },
      error: error => {
        console.error('Error removing product from cart:', error);
        this.isCartLoading = false;
      }
    });
  }

  getCartQuantity(productId: number): number {
    return this.cartQuantities[productId] || 0;
  }

  getTotalItems(): number {
    return this.cartItemCount;
  }

  getTotalAmount(): string {
    return this.cartTotalAmount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
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

  formatPrice(price: number): string {
    return price.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private loadProducts(): void {
    if (!this.supplierId) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.catalogService.searchProducts({
      supplierId: this.supplierId,
      categoryId: this.selectedCategoryId ? Number(this.selectedCategoryId) : undefined,
      minPrice: this.minPrice ? Number(this.minPrice) : undefined,
      maxPrice: this.maxPrice ? Number(this.maxPrice) : undefined,
      search: this.searchQuery || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: response => {
        this.products = response.content;
        this.currentPage = response.number;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;

        if (!this.supplierName && response.content.length > 0) {
          this.supplierName = response.content[0].supplierName || `Поставщик #${this.supplierId}`;
        }

        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading supplier catalog:', error);
        this.errorMessage = 'Не удалось загрузить каталог поставщика';
        this.products = [];
        this.totalPages = 0;
        this.totalElements = 0;
        this.isLoading = false;
      }
    });
  }

  private loadCategories(): void {
    this.catalogService.getCategories().subscribe({
      next: categories => {
        this.categories = categories;
      },
      error: error => console.error('Error loading categories for supplier catalog:', error)
    });
  }

  private loadSupplierName(): void {
    this.supplierName = `Поставщик #${this.supplierId}`;

    this.partnershipService.getCustomerSuppliers().subscribe({
      next: suppliers => {
        const supplier = suppliers.find(item => item.companyId === this.supplierId);
        if (supplier?.companyName) {
          this.supplierName = supplier.companyName;
        }
      },
      error: error => console.error('Error loading supplier name:', error)
    });
  }

  private loadCart(): void {
    this.cartService.getCart().subscribe({
      next: cart => this.syncCart(cart),
      error: error => console.error('Error loading retail cart:', error)
    });
  }

  private syncCart(cart: { items: Array<{ productId: number; quantity: number }>; itemCount: number; totalAmount: number }): void {
    this.cartQuantities = cart.items.reduce<Record<number, number>>((accumulator, item) => {
      accumulator[item.productId] = item.quantity;
      return accumulator;
    }, {});
    this.cartItemCount = cart.itemCount;
    this.cartTotalAmount = cart.totalAmount;
  }
}

