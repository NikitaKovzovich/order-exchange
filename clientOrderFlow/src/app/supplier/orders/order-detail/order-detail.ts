import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../services/order.service';
import { DocumentService } from '../../../services/document.service';
import { Order, OrderStatus } from '../../../models/api.models';

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
  rejectionReason: string = '';

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
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error confirming order:', error)
    });
  }

  openRejectModal() {
    this.showRejectModal = true;
  }

  closeRejectModal() {
    this.showRejectModal = false;
    this.rejectionReason = '';
  }

  rejectOrder() {
    if (!this.rejectionReason.trim()) return;
    this.orderService.rejectOrder(this.orderId, { reason: this.rejectionReason }).subscribe({
      next: () => {
        this.router.navigate(['/supplier/orders']);
      },
      error: (error) => console.error('Error rejecting order:', error)
    });
  }

  verifyPayment() {
    if (!this.order) return;
    this.orderService.confirmPayment(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error verifying payment:', error)
    });
  }

  rejectPayment() {
    const reason = prompt('Укажите причину отклонения оплаты:');
    if (!reason) return;
    this.orderService.rejectPayment(this.orderId, reason).subscribe({
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error rejecting payment:', error)
    });
  }

  markAsShipped() {
    if (!this.order) return;
    this.orderService.shipOrder(this.orderId).subscribe({
      next: (order) => {
        this.order = order;
      },
      error: (error) => console.error('Error shipping order:', error)
    });
  }

  generateTTN() {
    this.documentService.generateTTN(this.orderId).subscribe({
      next: (doc) => {
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
      },
      error: (error) => console.error('Error generating TTN:', error)
    });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('ru-RU');
  }

  formatAmount(amount: number): string {
    return amount.toLocaleString('ru-RU', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
}
