import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';

interface Product {
  id: number;
  name: string;
  supplier: string;
  category: string;
  price: string;
  status: string;
  statusClass: string;
}

interface Category {
  id: number;
  name: string;
  productCount: number;
}

@Component({
  selector: 'admin-content',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './content.html',
  styleUrls: ['./content.css']
})
export class Content {
  activeTab: string = 'products';

  searchQuery: string = '';
  selectedCategory: string = '';
  selectedSupplier: string = '';

  products: Product[] = [
    { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', supplier: 'Продукты Оптом', category: 'Молочные продукты', price: '2.50 BYN', status: 'Активен', statusClass: 'bg-green-100 text-green-800' },
    { id: 2, name: 'Запрещенный товар', supplier: 'Сомнительный Поставщик', category: 'Прочее', price: '100.00 BYN', status: 'Скрыт', statusClass: 'bg-gray-200 text-gray-800' }
  ];

  categories: Category[] = [
    { id: 1, name: 'Молочные продукты', productCount: 15 },
    { id: 2, name: 'Хлебобулочные изделия', productCount: 8 },
    { id: 3, name: 'Сыры', productCount: 12 }
  ];

  filteredProducts: Product[] = [...this.products];
  showDeleteModal: boolean = false;
  showHideModal: boolean = false;
  showNewCategoryModal: boolean = false;
  selectedItem: any = null;
  newCategoryName: string = '';

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  applyFilters() {
    this.filteredProducts = this.products.filter(product => {
      let match = true;

      if (this.searchQuery) {
        match = match && product.name.toLowerCase().includes(this.searchQuery.toLowerCase());
      }

      if (this.selectedCategory) {
        match = match && product.category === this.selectedCategory;
      }

      if (this.selectedSupplier) {
        match = match && product.supplier === this.selectedSupplier;
      }

      return match;
    });
  }

  openHideModal(product: Product) {
    this.selectedItem = product;
    this.showHideModal = true;
  }

  closeHideModal() {
    this.showHideModal = false;
    this.selectedItem = null;
  }

  confirmHide() {
    if (this.selectedItem) {
      this.selectedItem.status = 'Скрыт';
      this.selectedItem.statusClass = 'bg-gray-200 text-gray-800';
    }
    this.closeHideModal();
  }

  showProduct(product: Product) {
    product.status = 'Активен';
    product.statusClass = 'bg-green-100 text-green-800';
  }

  openDeleteModal(item: any) {
    this.selectedItem = item;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.selectedItem = null;
  }

  confirmDelete() {
    if (this.selectedItem) {
      if (this.activeTab === 'products') {
        const index = this.products.indexOf(this.selectedItem);
        if (index > -1) {
          this.products.splice(index, 1);
          this.applyFilters();
        }
      } else if (this.activeTab === 'categories') {
        const index = this.categories.indexOf(this.selectedItem);
        if (index > -1) {
          this.categories.splice(index, 1);
        }
      }
    }
    this.closeDeleteModal();
  }

  openNewCategoryModal() {
    this.showNewCategoryModal = true;
    this.newCategoryName = '';
  }

  closeNewCategoryModal() {
    this.showNewCategoryModal = false;
    this.newCategoryName = '';
  }

  addCategory() {
    if (this.newCategoryName.trim()) {
      const newCategory: Category = {
        id: this.categories.length + 1,
        name: this.newCategoryName,
        productCount: 0
      };
      this.categories.push(newCategory);
      this.closeNewCategoryModal();
    }
  }

  editCategory(category: Category) {
    console.log('Edit category:', category);
    // TODO: Implement edit logic
  }
}
