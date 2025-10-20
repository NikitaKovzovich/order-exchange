import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';

@Component({
  selector: 'app-order-detail',
  imports: [CommonModule, RouterLink],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.css'
})
export class OrderDetail implements OnInit {
  orderId: string = '';

  order = {
    id: '12345',
    supplier: 'Продукты Оптом',
    date: '30.09.2025',
    deliveryDate: '05.10.2025',
    amount: '8 240,00',
    status: 'in-transit',
    statusLabel: 'В пути',
    trackingNumber: 'TRK-2025-09-30-001',
    items: [
      { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', quantity: 100, price: '2.50', total: '250.00' },
      { id: 2, name: 'Сыр "Российский" весовой', quantity: 20, price: '18.00', total: '360.00' },
      { id: 3, name: 'Творог "Домашний" 9%', quantity: 50, price: '4.80', total: '240.00' }
    ],
    timeline: [
      { date: '30.09.2025 10:00', status: 'Заказ создан', description: 'Заказ оформлен и отправлен поставщику' },
      { date: '30.09.2025 14:30', status: 'Заказ подтвержден', description: 'Поставщик подтвердил наличие товаров' },
      { date: '01.10.2025 09:00', status: 'Заказ оплачен', description: 'Оплата подтверждена' },
      { date: '02.10.2025 15:00', status: 'Заказ отгружен', description: 'Товар передан в доставку' },
      { date: '03.10.2025 08:00', status: 'В пути', description: 'Заказ находится в пути', active: true }
    ],
    contact: {
      name: 'Иван Петров',
      phone: '+375 29 123-45-67',
      email: 'i.petrov@produkty-optom.by'
    },
    documents: [
      { name: 'Счет на оплату', type: 'invoice', date: '30.09.2025' },
      { name: 'УПД', type: 'upd', date: '02.10.2025' }
    ]
  };

  constructor(private route: ActivatedRoute) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.orderId = params['id'];
    });
  }

  getStatusClass(status: string): string {
    const classes: { [key: string]: string } = {
      'pending': 'bg-yellow-100 text-yellow-800',
      'awaiting-payment': 'bg-orange-100 text-orange-800',
      'paid': 'bg-blue-100 text-blue-800',
      'in-transit': 'bg-indigo-100 text-indigo-800',
      'delivered': 'bg-green-100 text-green-800',
      'closed': 'bg-gray-100 text-gray-800',
      'rejected': 'bg-red-100 text-red-800',
      'payment-issue': 'bg-red-100 text-red-800'
    };
    return classes[status] || 'bg-gray-100 text-gray-800';
  }

  confirmDelivery() {
    console.log('Подтверждение доставки заказа:', this.orderId);
  }

  reportIssue() {
    console.log('Сообщение о проблеме с заказом:', this.orderId);
  }

  downloadDocument(docType: string) {
    console.log('Скачивание документа:', docType);
  }

  cancelOrder() {
    if (confirm('Вы уверены, что хотите отменить заказ?')) {
      console.log('Отмена заказа:', this.orderId);
    }
  }

  contactSupplier() {
    console.log('Связаться с поставщиком');
  }
}
