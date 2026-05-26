import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CatalogService } from '../../../services/catalog.service';
import { CartService } from '../../../services/cart.service';
import { Product, ProductImage } from '../../../models/api.models';

interface UiNotification {
  type: 'success' | 'error';
  message: string;
}

@Component({
  selector: 'app-retail-product-view',
  imports: [CommonModule, FormsModule],
  templateUrl: './product-view.html',
  styleUrl: './product-view.css'
})
export class ProductView implements OnInit {
  productId: number = 0;
  product: Product | null = null;
  images: ProductImage[] = [];
  selectedPhoto: number = 0;
  quantity: number = 1;
  isLoading: boolean = false;
  isAddingToCart: boolean = false;
  errorMessage: string = '';
  notification: UiNotification | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogService: CatalogService,
    private cartService: CartService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Некорректный идентификатор товара.';
      return;
    }

    this.productId = id;
    this.loadProduct();
  }

  selectPhoto(index: number) {
    this.selectedPhoto = index;
  }

  get galleryImages(): string[] {
    const urls = this.images.map(image => image.imageUrl).filter((url): url is string => Boolean(url));
    if (urls.length > 0) {
      return urls;
    }
    return this.product?.primaryImageUrl ? [this.product.primaryImageUrl] : [];
  }

  addToCart() {
    if (!this.product) {
      return;
    }

    if (this.quantity < 1 || Number.isNaN(this.quantity)) {
      this.notification = { type: 'error', message: 'Укажите корректное количество.' };
      return;
    }

    this.isAddingToCart = true;
    this.cartService.addItem({
      productId: this.product.id,
      supplierId: this.product.supplierId,
      productName: this.product.name,
      productSku: this.product.sku,
      quantity: this.quantity,
      unitPrice: this.product.pricePerUnit,
      vatRate: this.product.vatRateValue
    }).subscribe({
      next: () => {
        this.isAddingToCart = false;
        this.notification = { type: 'success', message: 'Товар добавлен в заказ.' };
      },
      error: (error) => {
        console.error('Error adding to cart:', error);
        this.isAddingToCart = false;
        this.notification = { type: 'error', message: error.error?.message || 'Не удалось добавить товар в заказ.' };
      }
    });
  }

  goBack() {
    this.router.navigate(['/retail/catalog']);
  }

  formatPrice(value?: number): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  formatDate(value?: string | null): string {
    return value ? new Date(value).toLocaleDateString('ru-RU') : '—';
  }

  formatDimensions(value?: string | null): string {
    if (!value) {
      return '—';
    }
    try {
      const parsed = JSON.parse(value);
      const parts = [parsed.length, parsed.width, parsed.height].filter(part => part != null && part !== '');
      return parts.length > 0 ? `${parts.join(' × ')} см` : '—';
    } catch {
      return value;
    }
  }

  private loadProduct(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      product: this.catalogService.getProductById(this.productId),
      images: this.catalogService.getProductImages(this.productId).pipe(catchError(() => of([])))
    }).subscribe({
      next: ({ product, images }) => {
        this.product = product;
        this.images = images;
        this.selectedPhoto = 0;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading product:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить карточку товара.';
        this.isLoading = false;
      }
    });
  }
}
