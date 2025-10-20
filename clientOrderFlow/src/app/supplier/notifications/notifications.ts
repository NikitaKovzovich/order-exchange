import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';

interface Notification {
  id: number;
  type: string;
  title?: string;
  message: string;
  time: string;
  read: boolean;
  orderId?: string;
  actionLabel?: string;
}

@Component({
  selector: 'app-notifications',
  imports: [CommonModule, RouterModule],
  templateUrl: './notifications.html',
  styleUrl: './notifications.css'
})
export class Notifications implements OnInit {
  notifications: Notification[] = [
    {
      id: 1,
      type: 'order',
      title: 'Новый заказ',
      message: 'Поступил новый заказ #10235 от Торговой сети "Сеть Магазинов"',
      time: '5 минут назад',
      read: false,
      orderId: '10235',
      actionLabel: 'Перейти к заказу'
    },
    {
      id: 2,
      type: 'delivery',
      title: 'Товар доставлен',
      message: 'Торговая сеть "Гипермаркет Центр" подтвердила получение товара по заказу #10230. Подписанные документы загружены в систему.',
      time: '2 часа назад',
      read: true,
      orderId: '10230',
      actionLabel: 'Посмотреть документы'
    },
    {
      id: 3,
      type: 'shipment',
      message: 'Отгрузочные документы по заказу #10232 успешно сформированы. Заказ готов к отправке. Пожалуйста, подтвердите отгрузку, как только товар покинет ваш склад.',
      time: 'Вчера, 18:30',
      read: true,
      orderId: '10232',
      actionLabel: 'Перейти к заказу'
    },
    {
      id: 4,
      type: 'payment',
      title: 'Оплата подтверждена',
      message: 'Оплата по заказу #10232 подтверждена. Следующий шаг — подготовка к отгрузке.',
      time: 'Вчера, 15:10',
      read: true,
      orderId: '10232',
      actionLabel: 'Сформировать документы'
    },
    {
      id: 5,
      type: 'payment-verification',
      message: 'Торговая сеть «Супермаркет "Угол"» загрузила подтверждение оплаты по заказу #10232. Пожалуйста, проверьте поступление средств и подтвердите оплату.',
      time: 'Вчера, 11:45',
      read: true,
      orderId: '10232',
      actionLabel: 'Проверить оплату'
    }
  ];

  constructor(private router: Router) {}

  ngOnInit() {}

  get unreadCount(): number {
    return this.notifications.filter(n => !n.read).length;
  }

  markAllAsRead() {
    this.notifications.forEach(n => n.read = true);
  }

  goToOrder(orderId: string) {
    this.router.navigate(['/supplier/orders', orderId]);
  }

  getIconColor(type: string): string {
    const colors: { [key: string]: string } = {
      'order': 'bg-blue-100 text-blue-500',
      'delivery': 'bg-green-100 text-green-500',
      'shipment': 'bg-purple-100 text-purple-500',
      'payment': 'bg-green-100 text-green-500',
      'payment-verification': 'bg-yellow-100 text-yellow-500'
    };
    return colors[type] || 'bg-gray-100 text-gray-500';
  }

  getIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'order': 'M9 2a1 1 0 000 2h2a1 1 0 100-2H9z M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3z',
      'delivery': 'M8 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0zM15 16.5a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z M3 4a1 1 0 00-1 1v10a1 1 0 001 1h1.05a2.5 2.5 0 014.9 0H10a1 1 0 001-1V5a1 1 0 00-1-1H3zM6 7h4v5H6V7z M11 5a1 1 0 011-1h2l2.5 3v4.5a1.5 1.5 0 01-1.5 1.5h-2.5a1 1 0 01-1-1V5z',
      'shipment': 'M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z',
      'payment': 'M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z',
      'payment-verification': 'M8.433 7.418c.158-.103.346-.195.574-.277.228-.082.48-.124.74-.124.26-.001.512.041.74.124.228.082.416.174.574.277a.5.5 0 01.166.623l-1.5 2.5a.5.5 0 01-.832.093l-1.5-2.5a.5.5 0 01.166-.623z M10 18a8 8 0 100-16 8 8 0 000 16zm0-2a6 6 0 100-12 6 6 0 000 12z'
    };
    return icons[type] || icons['order'];
  }
}

