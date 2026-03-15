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
        loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard)
      },
      {
        path: 'catalog',
        loadComponent: () => import('./catalog/catalog').then(m => m.Catalog)
      },
      {
        path: 'cart',
        loadComponent: () => import('./cart/cart').then(m => m.Cart)
      },
      {
        path: 'suppliers',
        loadComponent: () => import('./suppliers/suppliers').then(m => m.Suppliers)
      },
      {
        path: 'suppliers/:id/catalog',
        loadComponent: () => import('./suppliers/supplier-catalog').then(m => m.SupplierCatalog)
      },
      {
        path: 'orders',
        loadComponent: () => import('./orders/orders').then(m => m.Orders)
      },
      {
        path: 'orders/:id',
        loadComponent: () => import('./orders/order-detail').then(m => m.OrderDetail)
      },
      {
        path: 'reception',
        loadComponent: () => import('./reception/reception').then(m => m.Reception)
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
        path: 'notifications',
        loadComponent: () => import('./notifications/notifications').then(m => m.Notifications)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];

