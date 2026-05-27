import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../services/order.service';
import { DocumentService } from '../../../services/document.service';
import { ChatService } from '../../../services/chat.service';
import { AuthService } from '../../../services/auth.service';
import { ChatMessage, DiscrepancyReport, GeneratedDocument, Order, OrderDocument, OrderHistoryEntry, OrderStatus } from '../../../models/api.models';
import { formatChatDateTime } from '../../../utils/chat-datetime.util';

interface TimelineItem {
  label: string;
  timestamp: string;
  current: boolean;
}

interface UnifiedDocument {
  key: string;
  name: string;
  generated: boolean;
  ref: OrderDocument | GeneratedDocument;
}

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './order-detail.html',
  styleUrls: ['./order-detail.css']
})
export class OrderDetail implements OnInit {
  readonly role: 'supplier' = 'supplier';
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
  timeline: TimelineItem[] = [];
  unifiedDocuments: UnifiedDocument[] = [];

  supplierUnp: string = '';
  customerUnp: string = '';

  currentUserId: number | null = null;
  chatMessages: ChatMessage[] = [];
  newMessage: string = '';
  isSendingMessage: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private documentService: DocumentService,
    private chatService: ChatService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.currentUserId = this.authService.getUserId();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.orderId = parseInt(id);
      this.loadOrder();
      this.loadChat();
    }
  }

  loadOrder() {
    this.isLoading = true;
    this.orderService.getOrderById(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.loadRelatedData();
        this.loadCompanyUnps();
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading order:', error);
        this.isLoading = false;
      }
    });
  }

  private loadCompanyUnps(): void {
    if (!this.order) {
      return;
    }
    this.authService.getCompanyProfile(this.order.supplierId).subscribe({
      next: profile => this.supplierUnp = profile.taxId || '',
      error: () => {}
    });
    this.authService.getCompanyProfile(this.order.customerId).subscribe({
      next: profile => this.customerUnp = profile.taxId || '',
      error: () => {}
    });
  }

  private loadChat(): void {
    this.chatService.getMessages(this.orderId).subscribe({
      next: page => {
        this.chatMessages = [...page.content].sort(
          (a, b) => new Date(a.sentAt).getTime() - new Date(b.sentAt).getTime()
        );
      },
      error: () => {
        this.chatMessages = [];
      }
    });
  }

  get isChatDisabled(): boolean {
    return this.order?.statusCode === 'CLOSED';
  }

  isOwnMessage(message: ChatMessage): boolean {
    return this.currentUserId != null && message.senderId === this.currentUserId;
  }

  sendMessage(): void {
    const text = this.newMessage.trim();
    if (!text || this.isChatDisabled) {
      return;
    }

    this.isSendingMessage = true;
    this.chatService.sendMessage(this.orderId, { messageText: text }).subscribe({
      next: message => {
        this.chatMessages = [...this.chatMessages, message];
        this.newMessage = '';
        this.isSendingMessage = false;
      },
      error: error => {
        console.error('Error sending message:', error);
        this.isSendingMessage = false;
      }
    });
  }

  get nextActionTitle(): string {
    return this.order?.statusCode === 'PENDING_CONFIRMATION' ? 'Действия' : 'Следующее действие';
  }

  get nextActionText(): string {
    switch (this.order?.statusCode) {
      case 'REJECTED':
        return 'Вы отклонили данный заказ. Вы можете закрыть заказ.';
      case 'AWAITING_PAYMENT':
        return 'Счёт на оплату сформирован и доступен торговой сети. Ожидается загрузка подтверждения оплаты.';
      case 'PENDING_PAYMENT_VERIFICATION':
        return 'Торговая сеть загрузила подтверждение оплаты. Оно доступно в блоке «Документы».';
      case 'PAYMENT_PROBLEM':
        return 'Возникли проблемы с оплатой. Ожидается загрузка нового платёжного поручения от Торговой сети.';
      case 'AWAITING_SHIPMENT':
        return this.order?.ttnGenerated ? 'Подтвердите отгрузку.' : 'Сформируйте ТТН.';
      case 'SHIPPED':
        return 'Товар отгружен и находится в пути. Ожидается подтверждение получения от Торговой сети.';
      case 'AWAITING_CORRECTION':
        return 'Оформите корректировку.';
      case 'DELIVERED':
        return 'Вы можете закрыть заказ.';
      case 'CLOSED':
        return 'Заказ выполнен и закрыт.';
      default:
        return '';
    }
  }

  private buildTimeline(): TimelineItem[] {
    const items = [...this.orderHistory]
      .map(entry => ({
        label: this.getHistoryDescription(entry),
        timestamp: this.getHistoryTimestamp(entry)
      }))
      .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

    return items.map((item, index) => ({ ...item, current: index === 0 }));
  }

  private buildUnifiedDocuments(): UnifiedDocument[] {
    const generated = this.generatedDocumentsForOrder.map(doc => ({
      key: `gen-${doc.id}`,
      name: this.generatedDocumentName(doc),
      generated: true,
      ref: doc
    }));
    const generatedKeys = new Set(this.generatedDocumentsForOrder.map(doc => doc.fileKey).filter(Boolean));
    const uploaded = this.orderDocuments
      .filter(doc => !doc.fileKey || !generatedKeys.has(doc.fileKey))
      .map(doc => ({
        key: `doc-${doc.id}`,
        name: doc.documentName || doc.documentTypeName || doc.originalFilename || doc.fileName || `Документ #${doc.id}`,
        generated: false,
        ref: doc
      }));
    return [...generated, ...uploaded];
  }

  private generatedDocumentName(doc: GeneratedDocument): string {
    const labels: { [key: string]: string } = {
      INVOICE: 'Счёт на оплату',
      TTN: 'ТТН',
      DISCREPANCY_ACT: 'Акт о расхождении',
      CORRECTION_TTN: 'Корректировочная ТТН'
    };
    const base = labels[doc.templateType] || doc.templateType;
    return doc.documentNumber ? `${base} № ${doc.documentNumber}` : base;
  }

  downloadUnifiedDocument(doc: UnifiedDocument): void {
    if (doc.generated) {
      this.downloadGeneratedDocument(doc.ref as GeneratedDocument);
    } else {
      this.downloadOrderDocument(doc.ref as OrderDocument);
    }
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
    if (orderDocument.fileKey) {
      this.documentService.getPresignedUrlByKey(orderDocument.fileKey).subscribe({
        next: url => {
          if (url) {
            window.open(url, '_blank');
          }
        },
        error: error => console.error('Error resolving document URL:', error)
      });
      return;
    }
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
    return formatChatDateTime(dateStr);
  }

  formatAmount(amount: number | undefined | null): string {
    return Number(amount ?? 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  private recomputeDerived(): void {
    this.timeline = this.buildTimeline();
    this.unifiedDocuments = this.buildUnifiedDocuments();
  }

  private loadRelatedData(): void {
    this.orderService.getOrderHistory(this.orderId).subscribe({
      next: history => {
        this.orderHistory = history;
        this.recomputeDerived();
      },
      error: error => console.error('Error loading order history:', error)
    });

    this.orderService.getOrderDocuments(this.orderId).subscribe({
      next: documents => {
        this.orderDocuments = documents;
        this.recomputeDerived();
      },
      error: error => console.error('Error loading order documents:', error)
    });

    this.documentService.getGeneratedDocumentsByOrder(this.orderId).subscribe({
      next: documents => {
        this.generatedDocuments = documents;
        this.recomputeDerived();
      },
      error: error => console.error('Error loading generated documents:', error)
    });

    this.orderService.getOrderDiscrepancies(this.orderId).subscribe({
      next: discrepancies => {
        this.discrepancies = discrepancies;
      },
      error: error => console.error('Error loading discrepancies:', error)
    });

    this.loadChat();
  }
}
