import { Routes } from '@angular/router';
import { Landing } from './landing/landing';
import { adminRoutes } from './admin/admin-routing-module';

export const routes: Routes = [
  {
    path: '',
    component: Landing
  },
  {
    path: 'admin',
    children: adminRoutes
  },
  {
    path: 'supplier',
    loadChildren: () => import('./supplier/supplier-module').then(m => m.SupplierModule)
  },
  {
    path: 'retail',
    loadChildren: () => import('./retail-network/retail-network-module').then(m => m.RetailNetworkModule)
  }
];
