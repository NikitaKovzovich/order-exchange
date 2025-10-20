import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';

interface DictionaryItem {
  id: number;
  value: string;
}

@Component({
  selector: 'admin-dictionaries',
  standalone: true,
  imports: [CommonModule, FormsModule, Header],
  templateUrl: './dictionaries.html',
  styleUrls: ['./dictionaries.css']
})
export class Dictionaries {
  activeTab: string = 'units';

  units: DictionaryItem[] = [
    { id: 1, value: 'шт' },
    { id: 2, value: 'кг' },
    { id: 3, value: 'упак' },
    { id: 4, value: 'л' }
  ];

  vatRates: DictionaryItem[] = [
    { id: 1, value: '20%' },
    { id: 2, value: '10%' },
    { id: 3, value: '0%' },
    { id: 4, value: 'Без НДС' }
  ];

  showEditModal: boolean = false;
  showDeleteModal: boolean = false;
  editingItem: DictionaryItem | null = null;
  selectedItem: DictionaryItem | null = null;
  editValue: string = '';

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  openEditModal(item?: DictionaryItem) {
    if (item) {
      this.editingItem = item;
      this.editValue = item.value;
    } else {
      this.editingItem = null;
      this.editValue = '';
    }
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
    this.editingItem = null;
    this.editValue = '';
  }

  saveItem() {
    if (this.editValue.trim()) {
      if (this.editingItem) {
        this.editingItem.value = this.editValue;
      } else {
        const newItem: DictionaryItem = {
          id: this.activeTab === 'units'
            ? this.units.length + 1
            : this.vatRates.length + 1,
          value: this.editValue
        };

        if (this.activeTab === 'units') {
          this.units.push(newItem);
        } else {
          this.vatRates.push(newItem);
        }
      }
      this.closeEditModal();
    }
  }

  openDeleteModal(item: DictionaryItem) {
    this.selectedItem = item;
    this.showDeleteModal = true;
  }

  closeDeleteModal() {
    this.showDeleteModal = false;
    this.selectedItem = null;
  }

  confirmDelete() {
    if (this.selectedItem) {
      if (this.activeTab === 'units') {
        const index = this.units.indexOf(this.selectedItem);
        if (index > -1) {
          this.units.splice(index, 1);
        }
      } else {
        const index = this.vatRates.indexOf(this.selectedItem);
        if (index > -1) {
          this.vatRates.splice(index, 1);
        }
      }
    }
    this.closeDeleteModal();
  }
}
