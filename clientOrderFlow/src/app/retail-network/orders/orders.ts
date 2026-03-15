import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { OrderService } from '../../services/order.service';
import { OrderDocumentDownloadService } from '../../services/order-document-download.service';
import { OrderStatus, OrderStatusSummary, OrderSummary } from '../../models/api.models';

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

@Component({
  selector: 'app-retail-orders',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './orders.html',
  styleUrl: './orders.css'
})
export class Orders implements OnInit {
  private readonly downloadingOrderIds = new Set<number>();
  notification: UiNotification | null = null;

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
    { value: 'CONFIRMED', label: 'Подтверждён' },
    { value: 'AWAITING_PAYMENT', label: 'Ожидает оплаты' },
    { value: 'PENDING_PAYMENT_VERIFICATION', label: 'Проверка оплаты' },
    { value: 'PAID', label: 'Оплачен' },
    { value: 'AWAITING_SHIPMENT', label: 'Ожидает отгрузки' },
    { value: 'SHIPPED', label: 'В пути' },
    { value: 'DELIVERED', label: 'Доставлен' },
    { value: 'PAYMENT_PROBLEM', label: 'Проблема с оплатой' },
    { value: 'AWAITING_CORRECTION', label: 'Ожидает корректировки' },
    { value: 'REJECTED', label: 'Отклонён' },
    { value: 'CLOSED', label: 'Закрыт' },
    { value: 'CANCELLED', label: 'Отменён' }
  ];

  constructor(
    private orderService: OrderService,
    private orderDocumentDownloadService: OrderDocumentDownloadService
  ) {}

  ngOnInit() {
    this.loadSummary();
    this.loadOrders();
  }

  loadOrders() {
    this.isLoading = true;
    this.orderService.getCustomerOrdersWithFilters({
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
        this.notification = {
          type: 'error',
          message: 'Не удалось загрузить список заказов. Попробуйте обновить страницу.'
        };
        this.isLoading = false;
      }
    });
  }

  loadSummary(): void {
    this.orderService.getCustomerSummary().subscribe({
      next: summary => {
        this.orderStats = summary;
      },
      error: error => console.error('Error loading customer summary:', error)
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

  clearNotification(): void {
    this.notification = null;
  }

  isDownloading(orderId: number): boolean {
    return this.downloadingOrderIds.has(orderId);
  }

  downloadOrderDocuments(order: OrderSummary): void {
    if (this.isDownloading(order.id)) {
      return;
    }

    this.clearNotification();
    this.downloadingOrderIds.add(order.id);

    this.orderDocumentDownloadService.downloadAllForOrder(order.id, order.orderNumber)
      .then(result => {
        if (result.status === 'success') {
          this.notification = {
            type: 'success',
            message: `Архив документов по заказу ${order.orderNumber} успешно скачан.`
          };
          return;
        }

        if (result.status === 'partial') {
          this.notification = {
            type: 'warning',
            message: `Архив по заказу ${order.orderNumber} скачан частично: ${result.downloadedCount} из ${result.totalRequested} документов добавлены.`
          };
          return;
        }

        if (result.status === 'empty') {
          this.notification = {
            type: 'info',
            message: 'Для этого заказа пока нет доступных документов для скачивания.'
          };
          return;
        }

        this.notification = {
          type: 'error',
          message: 'Не удалось подготовить архив документов заказа. Попробуйте ещё раз.'
        };
      })
      .catch(error => {
        console.error('Error downloading order documents archive:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось подготовить архив документов заказа. Попробуйте ещё раз.'
        };
      })
      .finally(() => {
        this.finishDownload(order.id);
      });
  }

  getStatusLabel(status: OrderStatus): string {
    const labels: { [key: string]: string } = {
      'PENDING_CONFIRMATION': 'Ожидает подтверждения',
      'CONFIRMED': 'Подтвержден',
      'REJECTED': 'Отклонен',
      'AWAITING_PAYMENT': 'Ожидает оплаты',
      'PENDING_PAYMENT_VERIFICATION': 'Проверка оплаты',
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
      'PENDING_CONFIRMATION': 'text-yellow-600 bg-yellow-200',
      'CONFIRMED': 'text-teal-600 bg-teal-200',
      'REJECTED': 'text-red-600 bg-red-200',
      'AWAITING_PAYMENT': 'text-orange-600 bg-orange-200',
      'PENDING_PAYMENT_VERIFICATION': 'text-purple-600 bg-purple-200',
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
    if (!dateStr) {
      return '—';
    }

    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  formatAmount(amount: number): string {
    if (!Number.isFinite(amount)) {
      return '0,00';
    }

    return amount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }


  private finishDownload(orderId: number): void {
    this.downloadingOrderIds.delete(orderId);
  }
}
