import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DocumentService } from '../../../services/document.service';
import { OrderService } from '../../../services/order.service';
import { GeneratedDocument, Order, OrderDocument } from '../../../models/api.models';

@Component({
  selector: 'supplier-invoice',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoice.html',
  styleUrls: ['./invoice.css']
})
export class Invoice implements OnInit {
  orderId: number = 0;
  order: Order | null = null;
  invoiceDocuments: OrderDocument[] = [];
  invoiceGeneratedDocuments: GeneratedDocument[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private documentService: DocumentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Некорректный идентификатор заказа.';
      return;
    }

    this.orderId = id;
    this.loadInvoiceData();
  }

  printInvoice() {
    window.print();
  }

  downloadGeneratedDocument(document: GeneratedDocument): void {
    this.documentService.downloadGeneratedDocument(document.id).subscribe({
      next: blob => this.saveBlob(blob, `invoice_${document.documentNumber}.pdf`),
      error: error => {
        console.error('Error downloading generated invoice:', error);
      }
    });
  }

  downloadOrderDocument(document: OrderDocument): void {
    this.documentService.downloadDocument(document.id).subscribe({
      next: blob => this.saveBlob(blob, document.originalFilename || document.fileName || `invoice-${document.id}`),
      error: error => {
        console.error('Error downloading invoice order document:', error);
      }
    });
  }

  goBack() {
    this.router.navigate(['/supplier/orders', this.orderId]);
  }

  formatDate(value?: string): string {
    return value ? new Date(value).toLocaleDateString('ru-RU') : '—';
  }

  formatAmount(value?: number): string {
    return Number(value || 0).toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  get invoiceWorkflowHint(): string {
    switch (this.order?.statusCode) {
      case 'PENDING_CONFIRMATION':
        return 'Счёт на оплату появится после подтверждения заказа поставщиком.';
      case 'CONFIRMED':
      case 'AWAITING_PAYMENT':
      case 'PENDING_PAYMENT_VERIFICATION':
      case 'PAID':
      case 'AWAITING_SHIPMENT':
      case 'SHIPPED':
      case 'DELIVERED':
      case 'CLOSED':
      case 'AWAITING_CORRECTION':
        return 'Счёт формируется автоматически в order flow и доступен здесь для просмотра и скачивания.';
      case 'REJECTED':
      case 'CANCELLED':
        return 'Для отклонённых или отменённых заказов счёт может отсутствовать.';
      default:
        return 'Здесь доступны автоматически сформированные документы счёта по заказу.';
    }
  }

  private loadInvoiceData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      order: this.orderService.getOrderById(this.orderId),
      orderDocuments: this.orderService.getOrderDocuments(this.orderId),
      generatedDocuments: this.documentService.getGeneratedDocumentsByOrder(this.orderId)
    }).subscribe({
      next: ({ order, orderDocuments, generatedDocuments }) => {
        this.order = order;
        this.invoiceDocuments = orderDocuments.filter(document => this.isInvoiceOrderDocument(document));
        this.invoiceGeneratedDocuments = generatedDocuments.filter(document => this.isInvoiceGeneratedDocument(document));
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading invoice data:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить данные счёта.';
        this.isLoading = false;
      }
    });
  }

  private isInvoiceOrderDocument(document: OrderDocument): boolean {
    const values = [document.documentType, document.documentTypeCode, document.documentTypeName]
      .filter((value): value is string => Boolean(value))
      .map(value => value.toUpperCase());

    return values.some(value => value.includes('INVOICE') || value.includes('СЧЕТ'));
  }

  private isInvoiceGeneratedDocument(document: GeneratedDocument): boolean {
    return document.templateType?.toUpperCase() === 'INVOICE';
  }

  private saveBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const anchor = window.document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    anchor.click();
    window.URL.revokeObjectURL(url);
  }
}
