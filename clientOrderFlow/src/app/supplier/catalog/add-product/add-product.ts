import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CatalogService } from '../../../services/catalog.service';
import { Category, Unit, VatRate, CreateProductRequest } from '../../../models/api.models';

@Component({
  selector: 'app-add-product',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './add-product.html',
  styleUrl: './add-product.css'
})
export class AddProduct implements OnInit {
  currentStep: number = 1;
  isLoading: boolean = false;
  isSaving: boolean = false;

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
    photos: [] as File[]
  };

  constructor(
    private router: Router,
    private catalogService: CatalogService
  ) {}

  ngOnInit() {
    this.loadReferenceData();
  }

  loadReferenceData() {
    this.isLoading = true;

    this.catalogService.getCategories().subscribe({
      next: (categories) => this.categories = categories,
      error: (error) => console.error('Error loading categories:', error)
    });

    this.catalogService.getUnits().subscribe({
      next: (units) => {
        this.units = units;
        if (units.length > 0) this.product.unitId = units[0].id;
      },
      error: (error) => console.error('Error loading units:', error)
    });

    this.catalogService.getVatRates().subscribe({
      next: (vatRates) => {
        this.vatRates = vatRates;
        if (vatRates.length > 0) this.product.vatRateId = vatRates[0].id;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading VAT rates:', error);
        this.isLoading = false;
      }
    });
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

  onFileSelect(event: any) {
    const files = Array.from(event.target.files) as File[];
    this.product.photos = files;
  }

  saveProduct() {
    this.isSaving = true;

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
      barcode: this.product.barcode || undefined
    };

    this.catalogService.createProduct(request).subscribe({
      next: (createdProduct) => {
        if (this.product.photos.length > 0) {
          this.uploadImages(createdProduct.id);
        } else {
          this.isSaving = false;
          this.router.navigate(['/supplier/catalog']);
        }
      },
      error: (error) => {
        console.error('Error creating product:', error);
        this.isSaving = false;
        alert('Ошибка при создании товара');
      }
    });
  }

  private uploadImages(productId: number) {
    let uploaded = 0;
    const total = this.product.photos.length;

    this.product.photos.forEach((file, index) => {
      this.catalogService.uploadProductImage(productId, file, index === 0).subscribe({
        next: () => {
          uploaded++;
          if (uploaded === total) {
            this.isSaving = false;
            this.router.navigate(['/supplier/catalog']);
          }
        },
        error: (error) => {
          console.error('Error uploading image:', error);
          uploaded++;
          if (uploaded === total) {
            this.isSaving = false;
            this.router.navigate(['/supplier/catalog']);
          }
        }
      });
    });
  }
}
