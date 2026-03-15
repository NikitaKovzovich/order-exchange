import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DocumentService } from '../../../services/document.service';
import { OrderService } from '../../../services/order.service';
import { GeneratedDocument, Order, OrderDocument } from '../../../models/api.models';

@Component({
  selector: 'supplier-upd',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './upd.html',
  styleUrls: ['./upd.css']
})
export class UPD implements OnInit {
  orderId: number = 0;
  order: Order | null = null;
  shippingDocuments: OrderDocument[] = [];
  generatedShippingDocuments: GeneratedDocument[] = [];
  isLoading: boolean = false;
  isProcessing: boolean = false;
  errorMessage: string = '';
  notification: { type: 'success' | 'error' | 'info' | 'warning'; message: string } | null = null;

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
    this.loadUpdData();
  }

  printUPD() {
    window.print();
  }

  downloadGeneratedDocument(document: GeneratedDocument): void {
    this.documentService.downloadGeneratedDocument(document.id).subscribe({
      next: blob => this.saveBlob(blob, `${document.templateType.toLowerCase()}_${document.documentNumber}.pdf`),
      error: error => {
        console.error('Error downloading generated shipping document:', error);
      }
    });
  }

  downloadOrderDocument(document: OrderDocument): void {
    this.documentService.downloadDocument(document.id).subscribe({
      next: blob => this.saveBlob(blob, document.originalFilename || document.fileName || `document-${document.id}`),
      error: error => {
        console.error('Error downloading shipping order document:', error);
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

  get canGenerateTtn(): boolean {
    return this.order?.statusCode === 'AWAITING_SHIPMENT';
  }

  get canGenerateCorrection(): boolean {
    return this.order?.statusCode === 'AWAITING_CORRECTION';
  }

  generateTtn(): void {
    if (!this.order) {
      return;
    }

    this.isProcessing = true;
    this.notification = null;
    this.orderService.generateTtn(this.orderId).subscribe({
      next: () => {
        this.notification = {
          type: 'success',
          message: 'ТТН успешно сформирована.'
        };
        this.isProcessing = false;
        this.loadUpdData();
      },
      error: error => {
        console.error('Error generating TTN from UPD screen:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось сформировать ТТН.'
        };
        this.isProcessing = false;
      }
    });
  }

  generateCorrection(): void {
    if (!this.order) {
      return;
    }

    this.isProcessing = true;
    this.notification = null;
    this.orderService.correctOrder(this.orderId).subscribe({
      next: () => {
        this.notification = {
          type: 'success',
          message: 'Корректировочный документ успешно сформирован.'
        };
        this.isProcessing = false;
        this.loadUpdData();
      },
      error: error => {
        console.error('Error generating correction document:', error);
        this.notification = {
          type: 'error',
          message: error.error?.message || 'Не удалось сформировать корректировочный документ.'
        };
        this.isProcessing = false;
      }
    });
  }

  private loadUpdData(): void {
    this.isLoading = true;
    this.errorMessage = '';

    forkJoin({
      order: this.orderService.getOrderById(this.orderId),
      orderDocuments: this.orderService.getOrderDocuments(this.orderId),
      generatedDocuments: this.documentService.getGeneratedDocumentsByOrder(this.orderId)
    }).subscribe({
      next: ({ order, orderDocuments, generatedDocuments }) => {
        this.order = order;
        this.shippingDocuments = orderDocuments.filter(document => this.isShippingOrderDocument(document));
        this.generatedShippingDocuments = generatedDocuments.filter(document => this.isShippingGeneratedDocument(document));
        this.isLoading = false;
      },
      error: error => {
        console.error('Error loading UPD data:', error);
        this.errorMessage = error.error?.message || 'Не удалось загрузить отгрузочные документы.';
        this.isLoading = false;
      }
    });
  }

  private isShippingOrderDocument(document: OrderDocument): boolean {
    const values = [document.documentType, document.documentTypeCode, document.documentTypeName]
      .filter((value): value is string => Boolean(value))
      .map(value => value.toUpperCase());

    return values.some(value => ['UPD', 'SIGNED_UPD', 'TTN', 'CORRECTION_TTN'].some(code => value.includes(code)));
  }

  private isShippingGeneratedDocument(document: GeneratedDocument): boolean {
    return ['TTN', 'CORRECTION_TTN', 'UPD'].includes(document.templateType?.toUpperCase());
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

