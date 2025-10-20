import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

interface CartItem {
  id: number;
  name: string;
  quantity: number;
  price: number;
  total: number;
}

interface OrderBySupplier {
  supplierId: number;
  supplierName: string;
  items: CartItem[];
  deliveryDate: string;
  deliveryAddress: string;
  total: number;
}

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cart.html',
  styleUrl: './cart.css'
})
export class Cart {
  ordersBySupplier: OrderBySupplier[] = [
    {
      supplierId: 1,
      supplierName: 'Продукты Оптом',
      deliveryDate: '',
      deliveryAddress: 'г. Минск, ул. Торговая, д. 5 (Основной склад)',
      items: [
        { id: 1, name: 'Молоко "Деревенское" 3.2% 1л', quantity: 100, price: 2.50, total: 250.00 },
        { id: 2, name: 'Вода питьевая негазированная 5л', quantity: 50, price: 3.20, total: 160.00 }
      ],
      total: 410.00
    },
    {
      supplierId: 2,
      supplierName: 'ХлебПром',
      deliveryDate: '',
      deliveryAddress: 'г. Минск, ул. Торговая, д. 5 (Основной склад)',
      items: [
        { id: 3, name: 'Хлеб "Бородинский" (нарезка)', quantity: 50, price: 1.80, total: 90.00 }
      ],
      total: 90.00
    }
  ];

  deliveryAddresses = [
    'г. Минск, ул. Торговая, д. 5 (Основной склад)',
    'г. Минск, пр. Победителей, д. 100 (Магазин №1)',
    'Добавить новый адрес...'
  ];

  constructor(private router: Router) {}

  get grandTotal(): number {
    return this.ordersBySupplier.reduce((sum, order) => sum + order.total, 0);
  }

  submitOrder(order: OrderBySupplier) {
    if (!order.deliveryDate) {
      alert('Пожалуйста, выберите дату доставки');
      return;
    }

    console.log('Submitting order:', order);
    alert(`Заказ отправлен поставщику "${order.supplierName}"!`);
    this.router.navigate(['/retail/orders']);
  }

  removeItem(orderId: number, itemId: number) {
    const order = this.ordersBySupplier.find(o => o.supplierId === orderId);
    if (order) {
      order.items = order.items.filter(item => item.id !== itemId);
      order.total = order.items.reduce((sum, item) => sum + item.total, 0);

      if (order.items.length === 0) {
        this.ordersBySupplier = this.ordersBySupplier.filter(o => o.supplierId !== orderId);
      }
    }
  }
}
