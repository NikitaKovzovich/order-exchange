import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Header } from '../../shared/header/header';
import { CatalogService } from '../../../services/catalog.service';
import { Product, ProductImage } from '../../../models/api.models';

@Component({
  selector: 'admin-product-view',
  standalone: true,
  imports: [CommonModule, RouterLink, Header],
  templateUrl: './product-view.html',
  styleUrls: ['./product-view.css']
})
export class ProductView implements OnInit {
  productId: number = 0;
  product: Product | null = null;
  images: ProductImage[] = [];
  selectedPhoto: number = 0;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private catalogService: CatalogService
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

  getStatusLabel(status?: string): string {
    switch (status) {
      case 'PUBLISHED': return 'Опубликован';
      case 'DRAFT': return 'Черновик';
      case 'HIDDEN': return 'Скрыт';
      case 'ARCHIVED': return 'В архиве';
      default: return status || '—';
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
      error: error => {
        console.error('Error loading product:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить карточку товара.';
        this.isLoading = false;
      }
    });
  }
}
