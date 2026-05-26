import { Routes } from '@angular/router';
import { authGuard } from '../guards/auth.guard';

export const retailNetworkRoutes: Routes = [
  {
    path: 'auth',
    children: [
      {
        path: 'registration',
        loadComponent: () => import('./auth/registration/registration').then(m => m.Registration)
      },
      {
        path: 'rejection',
        loadComponent: () => import('./auth/rejection/rejection').then(m => m.Rejection)
      },
      { path: '', redirectTo: 'registration', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    loadComponent: () => import('./shared/layout/layout').then(m => m.Layout),
    canActivate: [authGuard],
    data: { roles: ['RETAIL_CHAIN'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard),
        data: { title: 'Главная' }
      },
      {
        path: 'catalog',
        loadComponent: () => import('./catalog/catalog').then(m => m.Catalog),
        data: { title: 'Каталог товаров' }
      },
      {
        path: 'catalog/:id',
        loadComponent: () => import('./catalog/product-view/product-view').then(m => m.ProductView),
        data: { title: 'Карточка товара' }
      },
      {
        path: 'cart',
        loadComponent: () => import('./cart/cart').then(m => m.Cart),
        data: { title: 'Формирование заказа' }
      },
      {
        path: 'suppliers',
        loadComponent: () => import('./suppliers/suppliers').then(m => m.Suppliers),
        data: { title: 'База поставщиков' }
      },
      {
        path: 'suppliers/:id/catalog',
        loadComponent: () => import('./suppliers/supplier-catalog').then(m => m.SupplierCatalog),
        data: { title: 'Каталог поставщика' }
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders/orders').then(m => m.Orders),
        data: { title: 'Заказы' }
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./orders/order-detail').then(m => m.OrderDetail),
        data: { title: 'Детали заказа' }
      },
      {
        path: 'reception',
        loadComponent: () => import('./reception/reception').then(m => m.Reception),
        data: { title: 'Журнал приёмки' }
      },
      {
        path: 'analytics',
        loadComponent: () => import('./analytics/analytics').then(m => m.Analytics),
        data: { title: 'Аналитика' }
      },
      {
        path: 'communications',
        loadComponent: () => import('./communications/communications').then(m => m.Communications),
        data: { title: 'Коммуникации' }
      },
      {
        path: 'support',
        loadComponent: () => import('./support/support').then(m => m.Support),
        data: { title: 'Поддержка' }
      },
      {
        path: 'support/create',
        loadComponent: () => import('./support/create-ticket/create-ticket').then(m => m.CreateTicket),
        data: { title: 'Новое обращение' }
      },
      {
        path: 'notifications',
        loadComponent: () => import('./notifications/notifications').then(m => m.Notifications),
        data: { title: 'Уведомления' }
      },
      {
        path: 'profile',
        loadComponent: () => import('./profile/profile').then(m => m.Profile),
        data: { title: 'Профиль' }
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];

