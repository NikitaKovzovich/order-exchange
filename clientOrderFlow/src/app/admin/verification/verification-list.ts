import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Header } from '../shared/header/header';

interface VerificationRequest {
  id: number;
  companyName: string;
  taxId: string;
  role: string;
  roleClass: string;
  date: string;
  status: string;
  statusClass: string;
}

@Component({
  selector: 'admin-verification-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, Header],
  templateUrl: './verification-list.html',
  styleUrls: ['./verification-list.css']
})
export class VerificationList implements OnInit {
  searchQuery: string = '';
  selectedRole: string = '';
  selectedStatus: string = '';

  requests: VerificationRequest[] = [
    { id: 1, companyName: 'Молочный Мир', taxId: '190123456', role: 'Поставщик', roleClass: 'bg-green-100 text-green-800', date: '16.10.2025', status: 'Ожидает проверки', statusClass: 'bg-yellow-100 text-yellow-800' },
    { id: 2, companyName: 'Супермаркет "Угол"', taxId: '199876543', role: 'Торговая сеть', roleClass: 'bg-blue-100 text-blue-800', date: '16.10.2025', status: 'Ожидает проверки', statusClass: 'bg-yellow-100 text-yellow-800' },
    { id: 3, companyName: 'Продукты Оптом', taxId: '191234567', role: 'Поставщик', roleClass: 'bg-green-100 text-green-800', date: '15.10.2025', status: 'Одобрена', statusClass: 'bg-green-100 text-green-800' },
    { id: 4, companyName: 'Быстрый Магазинчик', taxId: '194567890', role: 'Торговая сеть', roleClass: 'bg-blue-100 text-blue-800', date: '15.10.2025', status: 'Отклонена', statusClass: 'bg-red-100 text-red-800' },
    { id: 5, companyName: 'ФруктТорг', taxId: '192345678', role: 'Поставщик', roleClass: 'bg-green-100 text-green-800', date: '14.10.2025', status: 'Ожидает проверки', statusClass: 'bg-yellow-100 text-yellow-800' }
  ];

  filteredRequests: VerificationRequest[] = [...this.requests];

  ngOnInit() {}

  applyFilters() {
    this.filteredRequests = this.requests.filter(request => {
      let match = true;

      if (this.searchQuery) {
        match = match && (
          request.companyName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          request.taxId.includes(this.searchQuery)
        );
      }

      if (this.selectedRole) {
        match = match && request.role === this.selectedRole;
      }

      if (this.selectedStatus) {
        match = match && request.status === this.selectedStatus;
      }

      return match;
    });
  }
}
