import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../services/order.service';
import { DocumentService } from '../../../services/document.service';
import { DiscrepancyReport, GeneratedDocument, Order, OrderDocument, OrderHistoryEntry, OrderStatus } from '../../../models/api.models';

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-detail.html',
  styleUrls: ['./order-detail.css']
})
export class OrderDetail implements OnInit {
  orderId: number = 0;
  order: Order | null = null;
  isLoading: boolean = false;
  showRejectModal: boolean = false;
  showRejectPaymentModal: boolean = false;
  rejectionReason: string = '';
  paymentRejectionReason: string = '';
  actionErrorMessage: string = '';
  orderHistory: OrderHistoryEntry[] = [];
  orderDocuments: OrderDocument[] = [];
  generatedDocuments: GeneratedDocument[] = [];
  discrepancies: DiscrepancyReport[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private documentService: DocumentService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.orderId = parseInt(id);
      this.loadOrder();
    }
  }

  loadOrder() {
    this.isLoading = true;
    this.orderService.getOrderById(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.loadRelatedData();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.isLoading = false;
      }
    });
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

  getStatusColor(status: OrderStatus): string {
    const colors: { [key: string]: string } = {
      'PENDING_CONFIRMATION': 'text-blue-600 bg-blue-200',
      'CONFIRMED': 'text-teal-600 bg-teal-200',
      'REJECTED': 'text-red-600 bg-red-200',
      'AWAITING_PAYMENT': 'text-yellow-600 bg-yellow-200',
      'PENDING_PAYMENT_VERIFICATION': 'text-purple-600 bg-purple-200',
      'PAID': 'text-green-600 bg-green-200',
      'PAYMENT_PROBLEM': 'text-red-600 bg-red-200',
      'AWAITING_SHIPMENT': 'text-orange-600 bg-orange-200',
      'SHIPPED': 'text-cyan-600 bg-cyan-200',
      'DELIVERED': 'text-indigo-600 bg-indigo-200',
      'AWAITING_CORRECTION': 'text-orange-600 bg-orange-200',
      'CLOSED': 'text-gray-600 bg-gray-200',
      'CANCELLED': 'text-gray-600 bg-gray-200'
    };
    return colors[status] || 'text-gray-600 bg-gray-200';
  }

  confirmOrder() {
    if (!this.order) return;
    this.orderService.confirmOrder(this.orderId).subscribe({
      next: () => this.loadOrder(),
      error: (error) => console.error('Error confirming order:', error)
    });
  }

  openRejectModal() {
    this.actionErrorMessage = '';
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectionReason = '';
    this.actionErrorMessage = '';
  }

  openRejectPaymentModal() {
    this.actionErrorMessage = '';
    this.showRejectPaymentModal = true;
  }

  closeRejectPaymentModal() {
    this.showRejectPaymentModal = false;
    this.paymentRejectionReason = '';
    this.actionErrorMessage = '';
  }

  rejectOrder() {
    if (!this.rejectionReason.trim()) {
      this.actionErrorMessage = 'Укажите причину отклонения заказа.';
      return;
    }

    this.actionErrorMessage = '';
    this.orderService.rejectOrder(this.orderId, { reason: this.rejectionReason }).subscribe({
      next: () => {
        this.closeRejectModal();
        this.router.navigate(['/supplier/orders']);
      },
      error: (error) => console.error('Error rejecting order:', error)
    });
  }

  verifyPayment() {
    if (!this.order) return;
    this.orderService.confirmPayment(this.orderId).subscribe({
      next: () => this.loadOrder(),
      error: (error) => console.error('Error verifying payment:', error)
    });
  }

  rejectPayment() {
    const reason = this.paymentRejectionReason.trim();
    if (!reason) {
      this.actionErrorMessage = 'Укажите причину отклонения оплаты.';
      return;
    }

    this.actionErrorMessage = '';
    this.orderService.rejectPayment(this.orderId, reason).subscribe({
      next: () => {
        this.closeRejectPaymentModal();
        this.loadOrder();
      },
      error: (error) => console.error('Error rejecting payment:', error)
    });
  }

  markAsShipped() {
    if (!this.order) return;
    this.orderService.shipOrder(this.orderId).subscribe({
      next: () => this.loadOrder(),
      error: (error) => console.error('Error shipping order:', error)
    });
  }

  closeOrder() {
    if (!this.order) return;
    this.orderService.closeOrder(this.orderId).subscribe({
      next: () => this.loadOrder(),
      error: error => console.error('Error closing order:', error)
    });
  }

  correctOrder() {
    if (!this.order) return;
    this.orderService.correctOrder(this.orderId).subscribe({
      next: () => this.loadOrder(),
      error: error => console.error('Error correcting order:', error)
    });
  }

  generateTTN() {
    this.orderService.generateTtn(this.orderId).subscribe({
      next: () => {
        this.loadOrder();
      },
      error: (error) => console.error('Error generating TTN:', error)
    });
  }

  downloadOrderDocument(orderDocument: OrderDocument): void {
    this.documentService.downloadDocument(orderDocument.id).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const anchor = window.document.createElement('a');
        anchor.href = url;
        anchor.download = orderDocument.originalFilename || orderDocument.fileName || `document-${orderDocument.id}`;
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: error => console.error('Error downloading order document:', error)
    });
  }

  downloadGeneratedDocument(document: GeneratedDocument): void {
    this.documentService.downloadGeneratedDocument(document.id).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const anchor = window.document.createElement('a');
        anchor.href = url;
        anchor.download = `${document.templateType.toLowerCase()}_${document.documentNumber}.pdf`;
        anchor.click();
        window.URL.revokeObjectURL(url);
      },
      error: error => console.error('Error downloading generated document:', error)
    });
  }

  getHistoryDescription(entry: OrderHistoryEntry): string {
    return entry.eventDescription || entry.description || 'Изменение статуса заказа';
  }

  getHistoryTimestamp(entry: OrderHistoryEntry): string {
    return entry.timestamp || entry.createdAt || '';
  }

  get generatedDocumentsForOrder(): GeneratedDocument[] {
    return this.generatedDocuments.filter(document => document.orderId === this.orderId);
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
      next: history => {
        this.orderHistory = history;
      },
      error: error => console.error('Error loading order history:', error)
    });

    this.orderService.getOrderDocuments(this.orderId).subscribe({
      next: documents => {
        this.orderDocuments = documents;
      },
      error: error => console.error('Error loading order documents:', error)
    });

    this.documentService.getGeneratedDocumentsByOrder(this.orderId).subscribe({
      next: documents => {
        this.generatedDocuments = documents;
      },
      error: error => console.error('Error loading generated documents:', error)
    });

    this.orderService.getOrderDiscrepancies(this.orderId).subscribe({
      next: discrepancies => {
        this.discrepancies = discrepancies;
      },
      error: error => console.error('Error loading discrepancies:', error)
    });
  }
}
