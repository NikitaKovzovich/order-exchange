import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CatalogService } from '../../../services/catalog.service';
import { Inventory, Product, ProductImage } from '../../../models/api.models';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

type ProductDetail = Product;

@Component({
  selector: 'app-product-card',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './product-card.html',
  styleUrl: './product-card.css'
})
export class ProductCard implements OnInit {
  productId: number = 0;
  product: ProductDetail | null = null;
  images: ProductImage[] = [];
  inventory: Inventory | null = null;
  isLoading: boolean = false;
  isSavingStock: boolean = false;
  errorMessage: string = '';
  newStock: number = 0;
  selectedPhoto: number = 0;
  notification: UiNotification | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogService: CatalogService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Некорректный идентификатор товара.';
      return;
    }

    this.productId = id;
    this.loadProductDetails();
  }

  selectPhoto(index: number) {
    this.selectedPhoto = index;
  }

  updateStock() {
    if (!this.product) {
      return;
    }

    if (this.newStock < 0 || Number.isNaN(this.newStock)) {
      this.notification = {
        type: 'error',
        message: 'Укажите корректное количество на складе.'
      };
      return;
    }

    this.isSavingStock = true;
    this.catalogService.updateInventory(this.product.id, { quantity: this.newStock }).subscribe({
      next: inventory => {
        this.inventory = inventory;
        this.product = {
          ...this.product!,
          quantityAvailable: inventory.quantityAvailable
        };
        this.newStock = inventory.quantityAvailable;
        this.notification = {
          type: 'success',
          message: 'Остаток успешно обновлён.'
        };
        this.isSavingStock = false;
      },
      error: error => {
        console.error('Error updating stock:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось обновить остаток.'
        };
        this.isSavingStock = false;
      }
    });
  }

  editProduct() {
    this.router.navigate(['/supplier/catalog', this.productId, 'edit']);
  }

  get currentStock(): number {
    return this.inventory?.quantityAvailable ?? this.product?.quantityAvailable ?? 0;
  }

  get galleryImages(): string[] {
    const imageUrls = this.images
      .map(image => image.imageUrl)
      .filter((url): url is string => Boolean(url));

    if (imageUrls.length > 0) {
      return imageUrls;
    }

    return this.product?.primaryImageUrl ? [this.product.primaryImageUrl] : [];
  }

  getStatusLabel(status?: string): string {
    switch (status) {
      case 'PUBLISHED':
        return 'Опубликован';
      case 'DRAFT':
        return 'Черновик';
      case 'ARCHIVED':
        return 'В архиве';
      default:
        return status || '—';
    }
  }

  getStatusClass(status?: string): string {
    switch (status) {
      case 'PUBLISHED':
        return 'bg-green-200 text-green-700';
      case 'DRAFT':
        return 'bg-yellow-200 text-yellow-700';
      case 'ARCHIVED':
        return 'bg-gray-200 text-gray-700';
      default:
        return 'bg-gray-200 text-gray-700';
    }
  }

  formatPrice(value?: number): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatDate(value?: string | null): string {
    if (!value) {
      return '—';
    }

    return new Date(value).toLocaleDateString('ru-RU');
  }

  private loadProductDetails(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      product: this.catalogService.getProductById(this.productId),
      images: this.catalogService.getProductImages(this.productId).pipe(catchError(() => of([]))),
      inventory: this.catalogService.getInventory(this.productId).pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ product, images, inventory }) => {
        this.product = product;
        this.images = images;
        this.inventory = inventory;
        this.newStock = inventory?.quantityAvailable ?? product.quantityAvailable;
        this.selectedPhoto = 0;
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading product detail:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить карточку товара.';
        this.isLoading = false;
      }
    });
  }
}
