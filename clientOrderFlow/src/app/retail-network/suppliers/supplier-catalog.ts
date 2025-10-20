import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-supplier-catalog',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './supplier-catalog.html',
  styleUrl: './supplier-catalog.css'
})
export class SupplierCatalog implements OnInit {
  supplierId: string = '';
  supplierName: string = 'Продукты Оптом';

  searchQuery: string = '';
  selectedCategory: string = '';
  minPrice: string = '';
  maxPrice: string = '';

  products = [
    { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', category: 'Молочные продукты', price: '2.50', stock: 150, unit: 'шт', image: '' },
    { id: 2, name: 'Сыр "Российский" весовой', category: 'Сыры', price: '18.00', stock: 25, unit: 'кг', image: '' },
    { id: 3, name: 'Творог "Домашний" 9%', category: 'Молочные продукты', price: '4.80', stock: 60, unit: 'шт', image: '' },
    { id: 4, name: 'Кефир 3.2%', category: 'Молочные продукты', price: '2.20', stock: 100, unit: 'шт', image: '' },
    { id: 5, name: 'Сметана 20%', category: 'Молочные продукты', price: '3.50', stock: 80, unit: 'шт', image: '' },
    { id: 6, name: 'Масло сливочное 82%', category: 'Молочные продукты', price: '12.50', stock: 45, unit: 'шт', image: '' }
  ];

  filteredProducts = [...this.products];
  categories = ['Все категории', 'Молочные продукты', 'Сыры'];

  cart: {[key: number]: number} = {};

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.supplierId = params['id'];
    });
  }

  applyFilters() {
    this.filteredProducts = this.products.filter(product => {
      let match = true;

      if (this.searchQuery) {
        match = match && product.name.toLowerCase().includes(this.searchQuery.toLowerCase());
      }

      if (this.selectedCategory && this.selectedCategory !== 'Все категории') {
        match = match && product.category === this.selectedCategory;
      }

      if (this.minPrice) {
        match = match && parseFloat(product.price) >= parseFloat(this.minPrice);
      }

      if (this.maxPrice) {
        match = match && parseFloat(product.price) <= parseFloat(this.maxPrice);
      }

      return match;
    });
  }

  resetFilters() {
    this.searchQuery = '';
    this.selectedCategory = '';
    this.minPrice = '';
    this.maxPrice = '';
    this.filteredProducts = [...this.products];
  }

  addToCart(productId: number) {
    if (!this.cart[productId]) {
      this.cart[productId] = 0;
    }
    this.cart[productId]++;
  }

  removeFromCart(productId: number) {
    if (this.cart[productId] && this.cart[productId] > 0) {
      this.cart[productId]--;
      if (this.cart[productId] === 0) {
        delete this.cart[productId];
      }
    }
  }

  getCartQuantity(productId: number): number {
    return this.cart[productId] || 0;
  }

  getTotalItems(): number {
    return Object.values(this.cart).reduce((sum, qty) => sum + qty, 0);
  }

  getTotalAmount(): string {
    let total = 0;
    for (const productId in this.cart) {
      const product = this.products.find(p => p.id === parseInt(productId));
      if (product) {
        total += parseFloat(product.price) * this.cart[productId];
      }
    }
    return total.toFixed(2);
  }
}

