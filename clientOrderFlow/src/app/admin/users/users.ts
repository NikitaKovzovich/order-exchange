import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Header } from '../shared/header/header';

interface User {
  id: number;
  name: string;
  taxId: string;
  email: string;
  role: string;
  status: string;
  registeredAt: string;
}

@Component({
  selector: 'admin-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, Header],
  templateUrl: './users.html',
  styleUrls: ['./users.css']
})
export class Users implements OnInit {
  activeTab: 'providers' | 'retailers' = 'providers';
  searchTerm: string = '';
  selectedStatus: string = '';

  providers: User[] = [
    { id: 1, name: 'Продукты Оптом', taxId: '191234567', email: 'contact@prodopt.by', role: 'supplier', status: 'active', registeredAt: '15.10.2025' },
    { id: 2, name: 'ФруктТорг', taxId: '192345678', email: 'info@frukt.by', role: 'supplier', status: 'active', registeredAt: '15.10.2025' },
    { id: 3, name: 'ХлебПром', taxId: '193456789', email: 'contact@hleb.by', role: 'supplier', status: 'blocked', registeredAt: '14.10.2025' }
  ];

  retailers: User[] = [
    { id: 4, name: 'Сеть Магазинов', taxId: '199876543', email: 'info@setmag.by', role: 'retail', status: 'active', registeredAt: '14.10.2025' },
    { id: 5, name: 'Гипермаркет "Центр"', taxId: '198765432', email: 'contact@center.by', role: 'retail', status: 'active', registeredAt: '13.10.2025' }
  ];

  filteredProviders: User[] = [];
  filteredRetailers: User[] = [];

  ngOnInit() {
    this.filterUsers();
  }

  setActiveTab(tab: 'providers' | 'retailers') {
    this.activeTab = tab;
    this.filterUsers();
  }

  filterUsers() {
    this.filteredProviders = this.providers.filter(user => {
      const matchesSearch = !this.searchTerm ||
        user.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.taxId.includes(this.searchTerm);
      const matchesStatus = !this.selectedStatus || user.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });

    this.filteredRetailers = this.retailers.filter(user => {
      const matchesSearch = !this.searchTerm ||
        user.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.taxId.includes(this.searchTerm);
      const matchesStatus = !this.selectedStatus || user.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });
  }
}
