import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { OrderStatus, OrderStatusSummary, OrderSummary } from '../../models/api.models';

@Component({
  selector: 'app-orders',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class Orders implements OnInit {
  orderStats: OrderStatusSummary = {
    totalOrders: 0,
    countByStatus: {},
    pendingConfirmation: 0,
    awaitingShipment: 0,
    inTransit: 0,
    paymentProblems: 0,
    awaitingDelivery: 0,
    requirePayment: 0,
    rejected: 0
  };

  filters = {
    search: '',
    status: '' as OrderStatus | '',
    dateFrom: '',
    dateTo: ''
  };

  orders: OrderSummary[] = [];
  isLoading: boolean = false;
  currentPage: number = 0;
  totalPages: number = 0;
  readonly pageSize: number = 20;
  readonly statusOptions: Array<{ value: OrderStatus; label: string }> = [
    { value: 'PENDING_CONFIRMATION', label: 'Ожидает подтверждения' },
    { value: 'AWAITING_PAYMENT', label: 'Ожидает оплаты' },
    { value: 'PENDING_PAYMENT_VERIFICATION', label: 'Проверка оплаты' },
    { value: 'AWAITING_SHIPMENT', label: 'Ожидает отгрузки' },
    { value: 'SHIPPED', label: 'В пути' },
    { value: 'DELIVERED', label: 'Доставлен' },
    { value: 'PAYMENT_PROBLEM', label: 'Проблема с оплатой' },
    { value: 'REJECTED', label: 'Отклонён' },
    { value: 'CLOSED', label: 'Закрыт' }
  ];

  constructor(private orderService: OrderService) {}

  ngOnInit() {
    this.loadSummary();
    this.loadOrders();
  }

  loadOrders() {
    this.isLoading = true;
    this.orderService.getSupplierOrdersWithFilters({
      status: this.filters.status || undefined,
      search: this.filters.search || undefined,
      dateFrom: this.filters.dateFrom || undefined,
      dateTo: this.filters.dateTo || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (response) => {
        this.orders = response.content;
        this.totalPages = response.totalPages;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.isLoading = false;
      }
    });
  }

  loadSummary(): void {
    this.orderService.getSupplierSummary().subscribe({
      next: summary => {
        this.orderStats = summary;
      },
      error: error => console.error('Error loading supplier summary:', error)
    });
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadOrders();
  }

  onStatusFilterChange() {
    this.currentPage = 0;
    this.loadOrders();
  }

  clearFilters(): void {
    this.filters = {
      search: '',
      status: '',
      dateFrom: '',
      dateTo: ''
    };
    this.currentPage = 0;
    this.loadOrders();
  }

  goToPreviousPage(): void {
    if (this.currentPage === 0) {
      return;
    }

    this.currentPage -= 1;
    this.loadOrders();
  }

  goToNextPage(): void {
    if (this.currentPage >= this.totalPages - 1) {
      return;
    }

    this.currentPage += 1;
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
