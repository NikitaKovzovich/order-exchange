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
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'catalog',
        loadComponent: () => import('./catalog/catalog').then(m => m.Catalog)
      },
      {
        path: 'catalog/empty',
        loadComponent: () => import('./catalog/empty-catalog/empty-catalog').then(m => m.EmptyCatalog)
      },
      {
        path: 'catalog/add',
        loadComponent: () => import('./catalog/add-product/add-product').then(m => m.AddProduct)
      },
      {
        path: 'catalog/:id/edit',
        loadComponent: () => import('./catalog/add-product/add-product').then(m => m.AddProduct)
      },
      {
        path: 'catalog/:id',
        loadComponent: () => import('./catalog/product-card/product-card').then(m => m.ProductCard)
      },
      {
        path: 'clients',
        loadComponent: () => import('./clients/clients').then(m => m.Clients)
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders/orders').then(m => m.Orders)
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./orders/order-detail/order-detail').then(m => m.OrderDetail)
      },
      {
        path: 'orders/:id/invoice',
        loadComponent: () => import('./orders/invoice/invoice').then(m => m.Invoice)
      },
      {
        path: 'orders/:id/upd',
        loadComponent: () => import('./orders/upd/upd').then(m => m.UPD)
      },
      {
        path: 'analytics',
        loadComponent: () => import('./analytics/analytics').then(m => m.Analytics)
      },
      {
        path: 'communications',
        loadComponent: () => import('./communications/communications').then(m => m.Communications)
      },
      {
        path: 'support',
        loadComponent: () => import('./support/support').then(m => m.Support)
      },
      {
        path: 'support/create',
        loadComponent: () => import('./support/create-ticket/create-ticket').then(m => m.CreateTicket)
      },
      {
        path: 'support/:id',
        loadComponent: () => import('./support/support-ticket/support-ticket').then(m => m.SupportTicket)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./notifications/notifications').then(m => m.Notifications)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];

