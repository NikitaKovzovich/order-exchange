import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-orders',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class Orders implements OnInit {
  orderStats = {
    new: 3,
    shipping: 5,
    inTransit: 12,
    problems: 1
  };

  filters = {
    orderId: '',
    client: '',
    status: '',
    date: ''
  };

  orders = [
    { id: '#12356', client: 'ООО "Сеть Магазинов"', date: '16.10.2025', amount: '1,250.00', status: 'pending' },
    { id: '#12355', client: 'ЧТУП "Продукты 24"', date: '15.10.2025', amount: '870.50', status: 'awaiting-shipping' },
    { id: '#12354', client: 'ОАО "Гипермаркет"', date: '15.10.2025', amount: '5,600.00', status: 'in-transit' },
    { id: '#12353', client: 'ООО "Сеть Магазинов"', date: '14.10.2025', amount: '3,110.00', status: 'payment-issue' },
    { id: '#12352', client: 'ЧТУП "Продукты 24"', date: '13.10.2025', amount: '950.00', status: 'closed' },
    { id: '#12351', client: 'ОАО "Гипермаркет"', date: '13.10.2025', amount: '2,400.00', status: 'awaiting-payment' },
    { id: '#12350', client: 'ООО "Сеть Магазинов"', date: '12.10.2025', amount: '780.00', status: 'delivered' },
    { id: '#12349', client: 'ЧТУП "Продукты 24"', date: '11.10.2025', amount: '1,500.00', status: 'paid' },
    { id: '#12348', client: 'ООО "Сеть Магазинов"', date: '10.10.2025', amount: '420.00', status: 'verifying-payment' },
    { id: '#12347', client: 'ОАО "Гипермаркет"', date: '10.10.2025', amount: '199.90', status: 'rejected' },
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

      if (this.filters.client) {
        match = match && order.client.toLowerCase().includes(this.filters.client.toLowerCase());
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
      'confirmed': 'Подтвержден',
      'rejected': 'Отклонен',
      'awaiting-payment': 'Ожидает оплаты',
      'verifying-payment': 'Ожидает проверки оплаты',
      'paid': 'Оплачен',
      'payment-issue': 'Проблема с оплатой',
      'awaiting-shipping': 'Ожидает отгрузки',
      'in-transit': 'В пути',
      'delivered': 'Доставлен',
      'closed': 'Закрыт'
    };
    return labels[status] || status;
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'pending': 'text-blue-600 bg-blue-200',
      'confirmed': 'text-teal-600 bg-teal-200',
      'rejected': 'text-red-600 bg-red-200',
      'awaiting-payment': 'text-yellow-600 bg-yellow-200',
      'verifying-payment': 'text-indigo-600 bg-indigo-200',
      'paid': 'text-green-600 bg-green-200',
      'payment-issue': 'text-red-600 bg-red-200',
      'awaiting-shipping': 'text-blue-600 bg-blue-200',
      'in-transit': 'text-cyan-600 bg-cyan-200',
      'delivered': 'text-indigo-600 bg-indigo-200',
      'closed': 'text-gray-600 bg-gray-200'
    };
    return classes[status] || 'text-gray-600 bg-gray-200';
  }
}
