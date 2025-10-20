import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-retail-catalog',
  imports: [CommonModule, FormsModule],
  templateUrl: './catalog.html',
  styleUrl: './catalog.css'
})
export class Catalog implements OnInit {
  searchQuery: string = '';
  selectedSupplier: string = '';
  selectedCategory: string = '';

  products = [
    { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', supplier: 'Продукты Оптом', price: '2.50', stock: 150, category: 'Молочные продукты' },
    { id: 2, name: 'Хлеб "Бородинский"', supplier: 'Хлебозавод №1', price: '1.80', stock: 80, category: 'Хлеб и выпечка' },
    { id: 3, name: 'Сыр "Российский" весовой', supplier: 'Продукты Оптом', price: '18.00', stock: 25, category: 'Молочные продукты' },
    { id: 4, name: 'Масло сливочное 82%', supplier: 'Молочная Ферма', price: '12.50', stock: 45, category: 'Молочные продукты' },
    { id: 5, name: 'Йогурт "Натуральный" 2.5%', supplier: 'Молочная Ферма', price: '3.20', stock: 100, category: 'Молочные продукты' },
    { id: 6, name: 'Творог "Домашний" 9%', supplier: 'Продукты Оптом', price: '4.80', stock: 60, category: 'Молочные продукты' }
  ];

  filteredProducts = [...this.products];
  suppliers = ['Все поставщики', 'Продукты Оптом', 'Хлебозавод №1', 'Молочная Ферма'];
  categories = ['Все категории', 'Молочные продукты', 'Хлеб и выпечка'];

  ngOnInit() {
  }

  applyFilters() {
    this.filteredProducts = this.products.filter(product => {
      let match = true;

      if (this.searchQuery) {
        match = match && product.name.toLowerCase().includes(this.searchQuery.toLowerCase());
      }

      if (this.selectedSupplier && this.selectedSupplier !== 'Все поставщики') {
        match = match && product.supplier === this.selectedSupplier;
      }

      if (this.selectedCategory && this.selectedCategory !== 'Все категории') {
        match = match && product.category === this.selectedCategory;
      }

      return match;
    });
  }

  addToCart(product: any) {
    console.log('Добавлено в корзину:', product);
  }
}

