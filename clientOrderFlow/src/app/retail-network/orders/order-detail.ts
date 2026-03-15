import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { DocumentService } from '../../services/document.service';
import { OrderDocumentDownloadService } from '../../services/order-document-download.service';
import { DiscrepancyReport, Order, OrderDocument, OrderHistoryEntry, OrderStatus } from '../../models/api.models';

interface DiscrepancyFormItem {
  orderItemId: number;
  productName: string;
  expectedQuantity: number;
  actualQuantity: number;
  reason: 'DAMAGE' | 'SHORTAGE' | 'WRONG_ITEM' | 'EXCESS' | 'OTHER';
}

interface UiNotification {
  type: 'success' | 'error' | 'info' | 'warning';
  message: string;
}

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
  isDownloadingAllDocuments: boolean = false;
  showCancelModal: boolean = false;
  showDiscrepancyModal: boolean = false;
  orderHistory: OrderHistoryEntry[] = [];
  orderDocuments: OrderDocument[] = [];
  discrepancies: DiscrepancyReport[] = [];
  notification: UiNotification | null = null;
  paymentProofFile: File | null = null;
  paymentReference: string = '';
  paymentNotes: string = '';
  discrepancyNotes: string = '';
  discrepancyItems: DiscrepancyFormItem[] = [];

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private documentService: DocumentService,
    private orderDocumentDownloadService: OrderDocumentDownloadService
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
        this.initializeDiscrepancyItems(order);
        this.loadRelatedData();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.notification = {
          type: 'error',
          message: 'Не удалось загрузить заказ. Попробуйте обновить страницу.'
        };
        this.isLoading = false;
      }
    });
  }

  clearNotification(): void {
    this.notification = null;
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
      next: () => this.loadOrder(),
      error: (error) => console.error('Error confirming delivery:', error)
    });
  }

  reportIssue() {
    if (this.order) {
      this.initializeDiscrepancyItems(this.order);
    }
    this.showDiscrepancyModal = true;
  }

  closeDiscrepancyModal(): void {
    this.showDiscrepancyModal = false;
  }

  submitDiscrepancy(): void {
    const changedItems = this.discrepancyItems.filter(item => item.actualQuantity !== item.expectedQuantity || item.reason !== 'SHORTAGE');
    if (changedItems.length === 0) {
      return;
    }

    this.orderService.createDiscrepancy(this.orderId, {
      items: changedItems.map(item => ({
        orderItemId: item.orderItemId,
        actualQuantity: item.actualQuantity,
        reason: item.reason
      })),
      notes: this.discrepancyNotes || undefined
    }).subscribe({
      next: () => {
        this.closeDiscrepancyModal();
        this.discrepancyNotes = '';
        this.loadOrder();
      },
      error: error => console.error('Error creating discrepancy:', error)
    });
  }

  onPaymentProofSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.paymentProofFile = input.files?.[0] || null;
  }

  uploadPaymentProof(): void {
    if (!this.paymentProofFile) {
      return;
    }

    this.orderService.uploadPaymentProofFile(this.orderId, this.paymentProofFile, this.paymentReference || undefined, this.paymentNotes || undefined).subscribe({
      next: () => {
        this.paymentProofFile = null;
        this.paymentReference = '';
        this.paymentNotes = '';
        this.loadOrder();
      },
      error: error => console.error('Error uploading payment proof:', error)
    });
  }

  repeatOrder(): void {
    this.orderService.repeatOrder(this.orderId).subscribe({
      next: () => {
        this.notification = {
          type: 'success',
          message: 'Товары из заказа добавлены в корзину.'
        };
      },
      error: error => console.error('Error repeating order:', error)
    });
  }

  downloadAllDocuments(): void {
    if (!this.order || this.isDownloadingAllDocuments) {
      return;
    }

    this.clearNotification();
    this.isDownloadingAllDocuments = true;

    this.orderDocumentDownloadService.downloadAllForOrder(this.order.id, this.order.orderNumber)
      .then(result => {
        if (result.status === 'success') {
          this.notification = {
            type: 'success',
            message: 'Архив всех документов заказа успешно скачан.'
          };
          return;
        }

        if (result.status === 'partial') {
          this.notification = {
            type: 'warning',
            message: `Архив скачан частично: ${result.downloadedCount} из ${result.totalRequested} документов добавлены.`
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
        this.isDownloadingAllDocuments = false;
      });
  }

  downloadDocument(document: OrderDocument) {
    this.documentService.downloadDocument(document.id).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const anchor = window.document.createElement('a');
        anchor.href = url;
        anchor.download = document.originalFilename || document.fileName || `document-${document.id}`;
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => console.error('Error downloading document:', error)
    });
  }

  cancelOrder() {
    this.showCancelModal = true;
  }

  confirmCancelOrder(): void {
    this.orderService.cancelOrder(this.orderId).subscribe({
      next: () => {
        this.showCancelModal = false;
        this.loadOrder();
      },
      error: (error) => console.error('Error cancelling order:', error)
    });
  }

  getHistoryDescription(entry: OrderHistoryEntry): string {
    return entry.eventDescription || entry.description || 'Изменение статуса заказа';
  }

  getHistoryTimestamp(entry: OrderHistoryEntry): string {
    return entry.timestamp || entry.createdAt || '';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  formatDateTime(dateStr: string): string {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleString('ru-RU');
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private loadRelatedData(): void {
    this.orderService.getOrderHistory(this.orderId).subscribe({
      next: history => this.orderHistory = history,
      error: error => console.error('Error loading order history:', error)
    });

    this.orderService.getOrderDocuments(this.orderId).subscribe({
      next: documents => this.orderDocuments = documents,
      error: error => console.error('Error loading order documents:', error)
    });

    this.orderService.getOrderDiscrepancies(this.orderId).subscribe({
      next: discrepancies => this.discrepancies = discrepancies,
      error: error => console.error('Error loading order discrepancies:', error)
    });
  }

  private initializeDiscrepancyItems(order: Order): void {
    this.discrepancyItems = order.items.map(item => ({
      orderItemId: item.id,
      productName: item.productName,
      expectedQuantity: item.quantity,
      actualQuantity: item.quantity,
      reason: 'SHORTAGE'
    }));
  }
}
