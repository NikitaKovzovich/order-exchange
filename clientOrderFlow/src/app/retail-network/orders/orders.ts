import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-retail-orders',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class Orders implements OnInit {
  filters = {
    orderId: '',
    supplier: '',
    status: ''
  };

  orders = [
    { id: '12345', supplier: 'Продукты Оптом', date: '30.09.2025', amount: '8 240,00', status: 'in-transit' },
    { id: '12344', supplier: 'Молочная Ферма', date: '30.09.2025', amount: '4 580,00', status: 'pending' },
    { id: '12343', supplier: 'Хлебозавод №1', date: '29.09.2025', amount: '2 450,00', status: 'delivered' },
    { id: '12342', supplier: 'Продукты Оптом', date: '29.09.2025', amount: '6 890,00', status: 'paid' },
    { id: '12341', supplier: 'Молочная Ферма', date: '28.09.2025', amount: '3 120,00', status: 'in-transit' }
  ];

  filteredOrders = [...this.orders];

  ngOnInit() {
  }

  applyFilters() {
    this.filteredOrders = this.orders.filter(order => {
      let match = true;

      if (this.filters.orderId) {
        match = match && order.id.includes(this.filters.orderId);
      }

      if (this.filters.supplier) {
        match = match && order.supplier.toLowerCase().includes(this.filters.supplier.toLowerCase());
      }

      if (this.filters.status) {
        match = match && order.status === this.filters.status;
      }

      return match;
    });
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'pending': 'Ожидает подтверждения',
      'paid': 'Оплачен',
      'in-transit': 'В пути',
      'delivered': 'Доставлен'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'pending': 'text-yellow-600 bg-yellow-200',
      'paid': 'text-green-600 bg-green-200',
      'in-transit': 'text-blue-600 bg-blue-200',
      'delivered': 'text-gray-600 bg-gray-200'
    };
    return classes[status] || 'text-gray-600 bg-gray-200';
  }
}
