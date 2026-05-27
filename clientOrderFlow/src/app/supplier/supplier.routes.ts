import { Routes } from '@angular/router';
import { authGuard } from '../guards/auth.guard';

export const supplierRoutes: Routes = [
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
    data: { roles: ['SUPPLIER'] },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard),
        data: { title: 'Главная' }
      },
      {
        path: 'catalog',
        loadComponent: () => import('./catalog/catalog').then(m => m.Catalog),
        data: { title: 'Управление каталогом' }
      },
      {
        path: 'catalog/empty',
        loadComponent: () => import('./catalog/empty-catalog/empty-catalog').then(m => m.EmptyCatalog),
        data: { title: 'Управление каталогом' }
      },
      {
        path: 'catalog/add',
        loadComponent: () => import('./catalog/add-product/add-product').then(m => m.AddProduct),
        data: { title: 'Создание нового товара' }
      },
      {
        path: 'catalog/:id/edit',
        loadComponent: () => import('./catalog/add-product/add-product').then(m => m.AddProduct),
        data: { title: 'Редактирование товара' }
      },
      {
        path: 'catalog/:id',
        loadComponent: () => import('./catalog/product-card/product-card').then(m => m.ProductCard),
        data: { title: 'Карточка товара' }
      },
      {
        path: 'clients',
        loadComponent: () => import('./clients/clients').then(m => m.Clients),
        data: { title: 'Мои клиенты' }
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders/orders').then(m => m.Orders),
        data: { title: 'Обработка заказов' }
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./orders/order-detail/order-detail').then(m => m.OrderDetail),
        data: { title: 'Детали заказа' }
      },
      {
        path: 'orders/:id/invoice',
        loadComponent: () => import('./orders/invoice/invoice').then(m => m.Invoice),
        data: { title: 'Счёт на оплату' }
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
        path: 'support/:id',
        loadComponent: () => import('./support/support-ticket/support-ticket').then(m => m.SupportTicket),
        data: { title: 'Обращение в поддержку' }
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

