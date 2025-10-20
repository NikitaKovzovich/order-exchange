import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

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

  products = [
    { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', sku: 'MLK-001', price: '2.50', stock: 150, unit: 'шт', status: 'Опубликован' },
    { id: 2, name: 'Хлеб "Бородинский"', sku: 'BRD-015', price: '1.80', stock: 80, unit: 'шт', status: 'Опубликован' },
    { id: 3, name: 'Сыр "Российский" весовой', sku: 'CHS-032', price: '18.00', stock: 25, unit: 'кг', status: 'Опубликован' },
    { id: 4, name: 'Масло сливочное 82%', sku: 'BTR-008', price: '12.50', stock: 45, unit: 'шт', status: 'Опубликован' },
    { id: 5, name: 'Йогурт "Натуральный" 2.5%', sku: 'YGT-022', price: '3.20', stock: 100, unit: 'шт', status: 'Черновик' },
    { id: 6, name: 'Творог "Домашний" 9%', sku: 'CTG-015', price: '4.80', stock: 60, unit: 'шт', status: 'Опубликован' },
  ];

  filteredProducts = [...this.products];

  ngOnInit() {
  }

  get hasDrafts(): boolean {
    return this.products.some(p => p.status === 'Черновик');
  }

  onSearch() {
    const query = this.searchQuery.toLowerCase().trim();
    if (!query) {
      this.filteredProducts = [...this.products];
    } else {
      this.filteredProducts = this.products.filter(product =>
        product.name.toLowerCase().includes(query) ||
        product.sku.toLowerCase().includes(query)
      );
    }
  }

  publishCatalog() {
    this.products.forEach(product => {
      if (product.status === 'Черновик') {
        product.status = 'Опубликован';
      }
    });
    this.filteredProducts = [...this.products];
    this.showDraftsAlert = false;
    alert('Каталог успешно обновлен! Все товары опубликованы.');
  }

  getStatusClass(status: string): string {
    switch(status) {
      case 'Опубликован':
        return 'text-green-600 bg-green-200';
      case 'Черновик':
        return 'text-yellow-600 bg-yellow-200';
      case 'Неопубликован':
        return 'text-gray-600 bg-gray-200';
      default:
        return 'text-gray-600 bg-gray-200';
    }
  }
}
