import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../services/order.service';
import { DocumentService } from '../../services/document.service';
import { ChatService } from '../../services/chat.service';
import { AuthService } from '../../services/auth.service';
import { OrderDocumentDownloadService } from '../../services/order-document-download.service';
import { ChatMessage, DiscrepancyReport, GeneratedDocument, Order, OrderDocument, OrderHistoryEntry, OrderStatus } from '../../models/api.models';
import { formatChatDateTime } from '../../utils/chat-datetime.util';

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

  readonly role: 'retail' = 'retail';
  supplierUnp: string = '';
  customerUnp: string = '';
  generatedDocuments: GeneratedDocument[] = [];
  currentUserId: number | null = null;
  chatMessages: ChatMessage[] = [];
  newMessage: string = '';
  isSendingMessage: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private documentService: DocumentService,
    private orderDocumentDownloadService: OrderDocumentDownloadService,
    private chatService: ChatService,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.currentUserId = this.authService.getUserId();
    this.route.params.subscribe(params => {
      this.orderId = parseInt(params['id']);
      this.loadOrder();
      this.loadChat();
    });
  }

  loadOrder() {
    this.isLoading = true;
    this.orderService.getOrderById(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
        this.initializeDiscrepancyItems(order);
        this.loadRelatedData();
        this.loadCompanyUnps();
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
    return formatChatDateTime(dateStr);
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

    this.documentService.getGeneratedDocumentsByOrder(this.orderId).subscribe({
      next: documents => this.generatedDocuments = documents,
      error: error => console.error('Error loading generated documents:', error)
    });

    this.orderService.getOrderDiscrepancies(this.orderId).subscribe({
      next: discrepancies => this.discrepancies = discrepancies,
      error: error => console.error('Error loading order discrepancies:', error)
    });

    this.loadChat();
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
      error: () => this.chatMessages = []
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

  get nextActionText(): string {
    switch (this.order?.statusCode) {
      case 'PENDING_CONFIRMATION':
        return 'Ваш заказ отправлен поставщику. Ожидайте подтверждения.';
      case 'REJECTED':
        return 'Поставщик отклонил ваш заказ. Причина указана в чате.';
      case 'AWAITING_PAYMENT':
        return 'Поставщик подтвердил ваш заказ и выставил счёт на оплату. Пожалуйста, оплатите заказ и загрузите подтверждающий документ (платёжное поручение).';
      case 'PENDING_PAYMENT_VERIFICATION':
        return 'Подтверждение оплаты загружено. Ожидайте проверки поступления средств со стороны поставщика.';
      case 'PAYMENT_PROBLEM':
        return 'Возникла проблема с подтверждением оплаты. Пожалуйста, проверьте чат заказа для получения подробностей от поставщика и загрузите корректный документ.';
      case 'AWAITING_SHIPMENT':
        return this.order?.ttnGenerated
          ? 'Поставщик сформировал отгрузочные документы (ТТН). Ожидается передача товара перевозчику.'
          : 'Оплата подтверждена. Поставщик готовит отгрузочные документы (ТТН).';
      case 'SHIPPED':
        return 'Товар отгружен. После физического получения товара нажмите кнопку «Принять товар».';
      case 'AWAITING_CORRECTION':
        return 'Вы заявили о расхождениях. Акт отправлен поставщику. Ожидайте, пока поставщик сформирует корректировочную накладную.';
      case 'DELIVERED':
        return 'Товар доставлен.';
      case 'CLOSED':
        return 'Заказ выполнен и закрыт.';
      default:
        return '';
    }
  }

  get isPaymentUploadStatus(): boolean {
    return this.order?.statusCode === 'AWAITING_PAYMENT' || this.order?.statusCode === 'PAYMENT_PROBLEM';
  }

  get timeline(): TimelineItem[] {
    const items = [...this.orderHistory]
      .map(entry => ({
        label: this.getHistoryDescription(entry),
        timestamp: this.getHistoryTimestamp(entry)
      }))
      .sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());
    return items.map((item, index) => ({ ...item, current: index === 0 }));
  }

  get unifiedDocuments(): UnifiedDocument[] {
    const generated = this.generatedDocuments
      .filter(doc => doc.orderId === this.orderId)
      .map(doc => ({ key: `gen-${doc.id}`, name: this.generatedDocumentName(doc), generated: true, ref: doc }));
    const uploaded = this.orderDocuments.map(doc => ({
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
      this.documentService.downloadGeneratedDocument((doc.ref as GeneratedDocument).id).subscribe({
        next: blob => this.saveBlob(blob, doc.name + '.pdf'),
        error: error => console.error('Error downloading generated document:', error)
      });
    } else {
      this.downloadDocument(doc.ref as OrderDocument);
    }
  }

  private saveBlob(blob: Blob, fileName: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = window.document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }

  get hasAcceptanceChanges(): boolean {
    return this.discrepancyItems.some(item => item.actualQuantity !== item.expectedQuantity);
  }

  get acceptanceButtonLabel(): string {
    return this.hasAcceptanceChanges ? 'Сформировать Акт о расхождении' : 'Подтвердить получение (Без расхождений)';
  }

  submitAcceptance(): void {
    if (this.hasAcceptanceChanges) {
      this.submitDiscrepancy();
    } else {
      this.closeDiscrepancyModal();
      this.confirmDelivery();
    }
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
