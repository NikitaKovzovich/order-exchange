import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

interface OrderItem {
  name: string;
  sku: string;
  quantity: number;
  price: number;
  total: number;
}

interface Order {
  id: string;
  date: string;
  status: 'pending' | 'awaiting-payment' | 'verifying-payment' | 'paid' | 'awaiting-shipping' | 'in-transit' | 'delivered';
  total: number;
  customerName: string;
  customerInn: string;
  supplierName: string;
  supplierInn: string;
  deliveryAddress: string;
  items: OrderItem[];
}

@Component({
  selector: 'app-order-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-detail.html',
  styleUrls: ['./order-detail.css']
})
export class OrderDetail implements OnInit {
  orderId: string = '';

  order: Order = {
    id: '10234',
    date: '2024-10-15',
    status: 'pending',
    total: 624.00,
    customerName: 'Открытое акционерное общество "Сеть Магазинов"',
    customerInn: '199876543',
    supplierName: 'Частное торговое унитарное предприятие "Продукты Оптом"',
    supplierInn: '191234567',
    deliveryAddress: 'г. Минск, ул. Торговая, д. 15',
    items: [
      { name: 'Молоко "Деревенское" 3.2% 1л', sku: 'MLK-001', quantity: 100, price: 2.50, total: 250.00 },
      { name: 'Хлеб "Бородинский"', sku: 'BRD-015', quantity: 80, price: 1.80, total: 144.00 },
      { name: 'Сыр "Российский"', sku: 'CHS-032', quantity: 10, price: 18.00, total: 180.00 },
      { name: 'Масло сливочное 82%', sku: 'BTR-008', quantity: 4, price: 12.50, total: 50.00 }
    ]
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.orderId = this.route.snapshot.paramMap.get('id') || '';
  }

  getStatusLabel(status: string): string {
    const labels: { [key: string]: string } = {
      'pending': 'Ожидает подтверждения',
      'awaiting-payment': 'Ожидает оплаты',
      'verifying-payment': 'Ожидает проверки оплаты',
      'paid': 'Оплачен',
      'awaiting-shipping': 'Ожидает отгрузки',
      'in-transit': 'В пути',
      'delivered': 'Доставлен'
    };
    return labels[status] || status;
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'pending': 'text-blue-600 bg-blue-200',
      'awaiting-payment': 'text-yellow-600 bg-yellow-200',
      'verifying-payment': 'text-purple-600 bg-purple-200',
      'paid': 'text-green-600 bg-green-200',
      'awaiting-shipping': 'text-orange-600 bg-orange-200',
      'in-transit': 'text-cyan-600 bg-cyan-200',
      'delivered': 'text-indigo-600 bg-indigo-200'
    };
    return colors[status] || 'text-gray-600 bg-gray-200';
  }

  confirmOrder() {
    this.order.status = 'awaiting-payment';
    alert('Заказ подтвержден. Ожидается оплата от клиента.');
  }

  rejectOrder() {
    if (confirm('Вы уверены, что хотите отклонить заказ?')) {
      alert('Заказ отклонен');
      this.router.navigate(['/supplier/orders']);
    }
  }

  verifyPayment() {
    this.order.status = 'paid';
    alert('Оплата подтверждена!');
  }

  markAsShipped() {
    this.order.status = 'in-transit';
    alert('Заказ отмечен как отгруженный и находится в пути.');
  }

  generateInvoice() {
    alert('Счет на оплату сформирован и отправлен клиенту.');
  }

  generateUPD() {
    alert('УПД сформирован и доступен для скачивания.');
  }
}
