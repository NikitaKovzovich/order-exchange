import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CatalogService } from '../../../services/catalog.service';
import { Category, Unit, VatRate, CreateProductRequest, Product, UpdateProductRequest } from '../../../models/api.models';

@Component({
  selector: 'app-add-product',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './add-product.html',
  styleUrl: './add-product.css'
})
export class AddProduct implements OnInit {
  private readonly maxPhotoSizeBytes = 5 * 1024 * 1024;
  private readonly allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/webp'];

  productId: number | null = null;
  isEditMode: boolean = false;
  currentStep: number = 1;
  isLoading: boolean = false;
  isSaving: boolean = false;
  errorMessage: string = '';

  categories: Category[] = [];
  units: Unit[] = [];
  vatRates: VatRate[] = [];

  product = {
    name: '',
    sku: '',
    categoryId: 0,
    description: '',
    pricePerUnit: 0,
    unitId: 0,
    vatRateId: 0,
    initialQuantity: 0,
    weight: 0,
    countryOfOrigin: '',
    barcode: '',
    packageLength: null as number | null,
    packageWidth: null as number | null,
    packageHeight: null as number | null,
    productionDate: '',
    expiryDate: '',
    photos: [] as File[]
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private catalogService: CatalogService
  ) {}

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.productId = id;
      this.isEditMode = true;
    }

    this.loadReferenceData();
  }

  loadReferenceData() {
    this.isLoading = true;

    forkJoin({
      categories: this.catalogService.getCategories(),
      units: this.catalogService.getUnits(),
      vatRates: this.catalogService.getVatRates()
    }).subscribe({
      next: ({ categories, units, vatRates }) => {
        this.categories = categories;
        this.units = units;
        this.vatRates = vatRates;

        if (!this.isEditMode) {
          if (units.length > 0) this.product.unitId = units[0].id;
          if (vatRates.length > 0) this.product.vatRateId = vatRates[0].id;
          this.isLoading = false;
          return;
        }

        this.loadProductForEdit();
      },
      error: (error) => {
        console.error('Error loading reference data:', error);
        this.errorMessage = 'Не удалось загрузить справочные данные';
        this.isLoading = false;
      }
    });
  }

  cancel() {
    if (this.isEditMode && this.productId) {
      this.router.navigate(['/supplier/catalog', this.productId]);
    } else {
      this.router.navigate(['/supplier/catalog']);
    }
  }

  nextStep() {
    if (this.currentStep < 3) {
      this.currentStep++;
    }
  }

  previousStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  onFileSelect(event: Event) {
    const input = event.target as HTMLInputElement;
    const selectedFiles = Array.from(input.files || []);

    if (selectedFiles.length === 0) {
      this.product.photos = [];
      return;
    }

    const invalidFile = selectedFiles.find(file => !this.isValidPhoto(file));
    if (invalidFile) {
      this.product.photos = [];
      input.value = '';
      this.errorMessage = this.getPhotoValidationError(invalidFile);
      return;
    }

    this.errorMessage = '';
    this.product.photos = selectedFiles;
  }

  saveProduct() {
    const validationError = this.validateProduct();
    if (validationError) {
      this.errorMessage = validationError.message;
      this.currentStep = validationError.step;
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';

    const request: CreateProductRequest = {
      name: this.product.name,
      sku: this.product.sku,
      categoryId: this.product.categoryId,
      description: this.product.description,
      pricePerUnit: this.product.pricePerUnit,
      unitId: this.product.unitId,
      vatRateId: this.product.vatRateId,
      initialQuantity: this.product.initialQuantity,
      weight: this.product.weight || undefined,
      countryOfOrigin: this.product.countryOfOrigin || undefined,
      barcode: this.product.barcode || undefined,
      packageDimensions: this.buildPackageDimensions(),
      productionDate: this.product.productionDate || undefined,
      expiryDate: this.product.expiryDate || undefined
    };

    if (this.isEditMode && this.productId) {
      const updateRequest: UpdateProductRequest = {
        ...request
      };

      this.catalogService.updateProduct(this.productId, updateRequest).subscribe({
        next: (updatedProduct) => {
          this.catalogService.updateInventory(updatedProduct.id, { quantity: this.product.initialQuantity }).subscribe({
            next: () => {
              if (this.product.photos.length > 0) {
                this.uploadImages(updatedProduct.id, true);
              } else {
                this.isSaving = false;
                this.router.navigate(['/supplier/catalog', updatedProduct.id]);
              }
            },
            error: error => {
              console.error('Error updating inventory:', error);
              this.isSaving = false;
              this.errorMessage = error.error?.message || 'Ошибка при обновлении остатка';
            }
          });
        },
        error: (error) => {
          console.error('Error updating product:', error);
          this.isSaving = false;
          this.errorMessage = error.error?.message || 'Ошибка при обновлении товара';
        }
      });
      return;
    }

    this.catalogService.createProduct(request).subscribe({
      next: (createdProduct) => {
        this.productId = createdProduct.id;
        this.isEditMode = true;

        if (this.product.photos.length > 0) {
          this.uploadImages(createdProduct.id, false);
        } else {
          this.isSaving = false;
          this.router.navigate(['/supplier/catalog']);
        }
      },
      error: (error) => {
        console.error('Error creating product:', error);
        this.isSaving = false;
        this.errorMessage = error.error?.message || 'Ошибка при создании товара';
      }
    });
  }

  get selectedPhotoNames(): string[] {
    return this.product.photos.map(file => file.name);
  }

  private validateProduct(): { message: string; step: number } | null {
    if (!this.product.name.trim()) {
      return { message: 'Укажите наименование товара.', step: 1 };
    }
    if (this.product.name.trim().length < 2) {
      return { message: 'Наименование товара должно содержать не менее 2 символов.', step: 1 };
    }
    if (!this.product.sku.trim()) {
      return { message: 'Укажите артикул (SKU) товара.', step: 1 };
    }
    if (!this.product.categoryId) {
      return { message: 'Выберите категорию товара.', step: 1 };
    }
    if (!this.product.pricePerUnit || this.product.pricePerUnit <= 0) {
      return { message: 'Укажите цену за единицу больше 0.', step: 2 };
    }
    if (!this.product.unitId) {
      return { message: 'Выберите единицу измерения.', step: 2 };
    }
    if (!this.product.vatRateId) {
      return { message: 'Выберите ставку НДС.', step: 2 };
    }
    if (this.product.initialQuantity == null || this.product.initialQuantity < 0) {
      return { message: 'Укажите корректное количество на складе.', step: 2 };
    }
    return null;
  }

  private buildPackageDimensions(): string | undefined {
    const length = this.product.packageLength;
    const width = this.product.packageWidth;
    const height = this.product.packageHeight;

    if (length == null && width == null && height == null) {
      return undefined;
    }

    return JSON.stringify({
      length: length ?? null,
      width: width ?? null,
      height: height ?? null
    });
  }

  private parsePackageDimensions(value?: string | null): void {
    this.product.packageLength = null;
    this.product.packageWidth = null;
    this.product.packageHeight = null;

    if (!value) {
      return;
    }

    try {
      const parsed = JSON.parse(value);
      this.product.packageLength = parsed.length ?? null;
      this.product.packageWidth = parsed.width ?? null;
      this.product.packageHeight = parsed.height ?? null;
    } catch {
      // legacy non-JSON value, leave dimensions empty
    }
  }

  private uploadImages(productId: number, returnToDetail: boolean): void {
    this.uploadImageAtIndex(productId, 0, returnToDetail);
  }

  private uploadImageAtIndex(productId: number, index: number, returnToDetail: boolean): void {
    if (index >= this.product.photos.length) {
      this.isSaving = false;
      this.router.navigate(returnToDetail ? ['/supplier/catalog', productId] : ['/supplier/catalog']);
      return;
    }

    const file = this.product.photos[index];
    this.catalogService.uploadProductImage(productId, file, index === 0).subscribe({
      next: () => {
        this.uploadImageAtIndex(productId, index + 1, returnToDetail);
      },
      error: (error) => {
        console.error('Error uploading image:', error);
        this.isSaving = false;
        this.errorMessage = this.extractImageUploadError(error, file.name);
      }
    });
  }

  private isValidPhoto(file: File): boolean {
    return this.allowedPhotoTypes.includes(file.type) && file.size <= this.maxPhotoSizeBytes;
  }

  private getPhotoValidationError(file: File): string {
    if (!this.allowedPhotoTypes.includes(file.type)) {
      return `Файл "${file.name}" имеет неподдерживаемый формат. Разрешены только JPEG, PNG и WebP.`;
    }

    if (file.size > this.maxPhotoSizeBytes) {
      return `Файл "${file.name}" превышает максимальный размер 5 MB.`;
    }

    return `Файл "${file.name}" не прошёл валидацию.`;
  }

  private extractImageUploadError(error: any, fileName: string): string {
    const backendMessage = error?.error?.message || error?.message;

    if (backendMessage) {
      return `Товар сохранён, но изображение "${fileName}" не загрузилось: ${backendMessage}`;
    }

    return `Товар сохранён, но изображение "${fileName}" не загрузилось. Проверьте формат (JPEG/PNG/WebP) и размер файла до 5 MB.`;
  }

  private loadProductForEdit(): void {
    if (!this.productId) {
      this.isLoading = false;
      return;
    }

    forkJoin({
      product: this.catalogService.getProductById(this.productId),
      inventory: this.catalogService.getInventory(this.productId)
    }).subscribe({
      next: ({ product, inventory }) => {
        this.populateProductForm(product, inventory.quantityAvailable);
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading product for edit:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить товар для редактирования';
        this.isLoading = false;
      }
    });
  }

  private populateProductForm(product: Product, quantityAvailable: number): void {
    this.product = {
      ...this.product,
      name: product.name,
      sku: product.sku,
      categoryId: product.category.id,
      description: product.description || '',
      pricePerUnit: product.pricePerUnit,
      unitId: this.resolveUnitId(product),
      vatRateId: this.resolveVatRateId(product),
      initialQuantity: quantityAvailable,
      weight: product.weight || 0,
      countryOfOrigin: product.countryOfOrigin || '',
      barcode: product.barcode || '',
      productionDate: product.productionDate ? product.productionDate.substring(0, 10) : '',
      expiryDate: product.expiryDate ? product.expiryDate.substring(0, 10) : '',
      photos: []
    };

    this.parsePackageDimensions(product.packageDimensions);
  }

  private resolveUnitId(product: Product): number {
    return this.units.find(unit => unit.name === product.unitOfMeasure)?.id || this.units[0]?.id || 0;
  }

  private resolveVatRateId(product: Product): number {
    const byDescription = this.vatRates.find(vat => vat.description === product.vatRateName)?.id;
    if (byDescription) {
      return byDescription;
    }

    return this.vatRates.find(vat => Number(vat.ratePercentage) === Number(product.vatRateValue))?.id || this.vatRates[0]?.id || 0;
  }
}
