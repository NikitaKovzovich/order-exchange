import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { DocumentService } from '../../services/document.service';
import { Order, OrderStatus } from '../../models/api.models';

@Component({
  selector: 'app-order-detail',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.css'
})
export class OrderDetail implements OnInit {
  orderId: number = 0;
  order: Order | null = null;
  isLoading: boolean = false;
  showDiscrepancyModal: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private documentService: DocumentService
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.orderId = parseInt(params['id']);
      this.loadOrder();
    });
  }

  loadOrder() {
    this.isLoading = true;
    this.orderService.getOrderById(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.isLoading = false;
      }
    });
  }

  getStatusClass(status: OrderStatus): string {
    const classes: { [key: string]: string } = {
      'PENDING_CONFIRMATION': 'bg-yellow-100 text-yellow-800',
      'CONFIRMED': 'bg-teal-100 text-teal-800',
      'REJECTED': 'bg-red-100 text-red-800',
      'AWAITING_PAYMENT': 'bg-orange-100 text-orange-800',
      'PENDING_PAYMENT_VERIFICATION': 'bg-purple-100 text-purple-800',
      'PAID': 'bg-blue-100 text-blue-800',
      'PAYMENT_PROBLEM': 'bg-red-100 text-red-800',
      'AWAITING_SHIPMENT': 'bg-blue-100 text-blue-800',
      'SHIPPED': 'bg-indigo-100 text-indigo-800',
      'DELIVERED': 'bg-green-100 text-green-800',
      'AWAITING_CORRECTION': 'bg-orange-100 text-orange-800',
      'CLOSED': 'bg-gray-100 text-gray-800',
      'CANCELLED': 'bg-gray-100 text-gray-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
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

  confirmDelivery() {
    if (!this.order) return;
    this.orderService.confirmDelivery(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error confirming delivery:', error)
    });
  }

  reportIssue() {
    this.showDiscrepancyModal = true;
  }

  downloadDocument(docType: string) {
    this.documentService.getEntityDocuments('ORDER', this.orderId).subscribe({
      next: (docs) => {
        const doc = docs.find(d => d.documentType === docType);
        if (doc) {
          this.documentService.downloadDocument(doc.id).subscribe({
            next: (blob) => {
              const url = window.URL.createObjectURL(blob);
              const a = document.createElement('a');
              a.href = url;
              a.download = doc.fileName;
              a.click();
              window.URL.revokeObjectURL(url);
            }
          });
        }
      },
      error: (error) => console.error('Error downloading document:', error)
    });
  }

  cancelOrder() {
    if (!confirm('Вы уверены, что хотите отменить заказ?')) return;

    this.orderService.cancelOrder(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error cancelling order:', error)
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
