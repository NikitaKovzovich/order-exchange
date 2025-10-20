import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-retail-suppliers',
  imports: [CommonModule, FormsModule],
  templateUrl: './suppliers.html',
  styleUrl: './suppliers.css'
})
export class Suppliers {
  searchQuery: string = '';
  selectedCategory: string = '';
  selectedRating: string = '';

  suppliers = [
    {
      id: 1,
      name: 'Продукты Оптом',
      category: 'Молочные продукты, Сыры',
      rating: 4.8,
      products: 156,
      status: 'active',
      contact: '+375 29 123-45-67'
    },
    {
      id: 2,
      name: 'Хлебозавод №1',
      category: 'Хлеб и выпечка',
      rating: 4.9,
      products: 45,
      status: 'active',
      contact: '+375 29 234-56-78'
    },
    {
      id: 3,
      name: 'Молочная Ферма',
      category: 'Молочные продукты',
      rating: 4.7,
      products: 89,
      status: 'active',
      contact: '+375 29 345-67-89'
    },
    {
      id: 4,
      name: 'Мясная Лавка',
      category: 'Мясная продукция',
      rating: 4.6,
      products: 67,
      status: 'active',
      contact: '+375 29 456-78-90'
    }
  ];

  filteredSuppliers = [...this.suppliers];

  categories = ['Все категории', 'Молочные продукты', 'Хлеб и выпечка', 'Мясная продукция', 'Сыры'];
  ratings = ['Все рейтинги', '4.5+', '4.7+', '4.9+'];

  applyFilters() {
    this.filteredSuppliers = this.suppliers.filter(supplier => {
      let match = true;

      if (this.searchQuery) {
        match = match && supplier.name.toLowerCase().includes(this.searchQuery.toLowerCase());
      }

      if (this.selectedCategory && this.selectedCategory !== 'Все категории') {
        match = match && supplier.category.includes(this.selectedCategory);
      }

      if (this.selectedRating && this.selectedRating !== 'Все рейтинги') {
        const minRating = parseFloat(this.selectedRating.replace('+', ''));
        match = match && supplier.rating >= minRating;
      }

      return match;
    });
  }

  resetFilters() {
    this.searchQuery = '';
    this.selectedCategory = '';
    this.selectedRating = '';
    this.filteredSuppliers = [...this.suppliers];
  }
}
