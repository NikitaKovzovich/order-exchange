import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

interface Product {
  name: string;
  sku: string;
  category: string;
  description: string;
  price: number;
  unit: string;
  vat: string;
  stock: number;
  weight: number;
  length: number;
  width: number;
  height: number;
  mfgDate: string;
  expDate: string;
  origin: string;
  photos: File[];
  status: 'draft' | 'published';
}

@Component({
  selector: 'app-add-product',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './add-product.html',
  styleUrl: './add-product.css'
})
export class AddProduct {
  currentStep: number = 1;

  product: Product = {
    name: '',
    sku: '',
    category: 'Молочные продукты',
    description: '',
    price: 0,
    unit: 'шт',
    vat: '20%',
    stock: 0,
    weight: 0,
    length: 0,
    width: 0,
    height: 0,
    mfgDate: '',
    expDate: '',
    origin: '',
    photos: [],
    status: 'draft'
  };

  constructor(private router: Router) {}

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
    this.product.status = 'draft';
    console.log('Saving product as draft:', this.product);

    alert('Товар успешно сохранен как черновик! Перейдите в каталог и нажмите "Обновить каталог" для публикации.');
    this.router.navigate(['/supplier/catalog']);
  }
}
