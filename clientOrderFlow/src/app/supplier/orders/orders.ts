import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { OrderSummary, OrderStatus } from '../../models/api.models';

@Component({
  selector: 'app-orders',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class Orders implements OnInit {
  orderStats = {
    new: 0,
    shipping: 0,
    inTransit: 0,
    problems: 0
  };

  filters = {
    orderId: '',
    client: '',
    status: '' as OrderStatus | '',
    date: ''
  };

  orders: OrderSummary[] = [];
  filteredOrders: OrderSummary[] = [];
  isLoading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.loadOrders();
  }

  loadOrders() {
    this.isLoading = true;
    const status = this.filters.status || undefined;

    this.orderService.getSupplierOrders(status, this.currentPage, 20).subscribe({
      next: (response) => {
        this.orders = response.content;
        this.filteredOrders = [...this.orders];
        this.totalPages = response.totalPages;
        this.updateStats();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.isLoading = false;
      }
    });
  }

  updateStats() {
    this.orderStats = {
      new: this.orders.filter(o => o.statusCode === 'PENDING_CONFIRMATION').length,
      shipping: this.orders.filter(o => o.statusCode === 'AWAITING_SHIPMENT').length,
      inTransit: this.orders.filter(o => o.statusCode === 'SHIPPED').length,
      problems: this.orders.filter(o => o.statusCode === 'PAYMENT_PROBLEM').length
    };
  }

  applyFilters() {
    this.filteredOrders = this.orders.filter(order => {
      let match = true;

      if (this.filters.orderId) {
        match = match && order.orderNumber.includes(this.filters.orderId);
      }

      if (this.filters.client) {
        match = match && (order.customerName?.toLowerCase().includes(this.filters.client.toLowerCase()) || false);
      }

      if (this.filters.status) {
        match = match && order.statusCode === this.filters.status;
      }

      return match;
    });
  }

  onStatusFilterChange() {
    this.currentPage = 0;
    this.loadOrders();
  }

  getStatusLabel(status: OrderStatus): string {
    const labels: { [key: string]: string } = {
      'PENDING_CONFIRMATION': 'Ожидает подтверждения',
      'CONFIRMED': 'Подтвержден',
      'REJECTED': 'Отклонен',
      'AWAITING_PAYMENT': 'Ожидает оплаты',
      'PENDING_PAYMENT_VERIFICATION': 'Ожидает проверки оплаты',
      'PAID': 'Оплачен',
      'PAYMENT_PROBLEM': 'Проблема с оплатой',
      'AWAITING_SHIPMENT': 'Ожидает отгрузки',
      'SHIPPED': 'В пути',
      'DELIVERED': 'Доставлен',
      'AWAITING_CORRECTION': 'Ожидает корректировки',
      'CLOSED': 'Закрыт',
      'CANCELLED': 'Отменен'
    };
    return labels[status] || status;
  }

  getStatusClass(status: OrderStatus): string {
    const classes: { [key: string]: string } = {
      'PENDING_CONFIRMATION': 'text-blue-600 bg-blue-200',
      'CONFIRMED': 'text-teal-600 bg-teal-200',
      'REJECTED': 'text-red-600 bg-red-200',
      'AWAITING_PAYMENT': 'text-yellow-600 bg-yellow-200',
      'PENDING_PAYMENT_VERIFICATION': 'text-indigo-600 bg-indigo-200',
      'PAID': 'text-green-600 bg-green-200',
      'PAYMENT_PROBLEM': 'text-red-600 bg-red-200',
      'AWAITING_SHIPMENT': 'text-blue-600 bg-blue-200',
      'SHIPPED': 'text-cyan-600 bg-cyan-200',
      'DELIVERED': 'text-indigo-600 bg-indigo-200',
      'AWAITING_CORRECTION': 'text-orange-600 bg-orange-200',
      'CLOSED': 'text-gray-600 bg-gray-200',
      'CANCELLED': 'text-gray-600 bg-gray-200'
    };
    return classes[status] || 'text-gray-600 bg-gray-200';
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
