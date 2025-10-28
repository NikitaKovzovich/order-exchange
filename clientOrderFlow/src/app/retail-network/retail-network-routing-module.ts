import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Registration } from './auth/registration/registration';
import { Rejection } from './auth/rejection/rejection';
import { Layout } from './shared/layout/layout';
import { Dashboard } from './dashboard/dashboard';
import { Catalog } from './catalog/catalog';
import { Orders } from './orders/orders';
import { OrderDetail } from './orders/order-detail';
import { Suppliers } from './suppliers/suppliers';
import { SupplierCatalog } from './suppliers/supplier-catalog';
import { Analytics } from './analytics/analytics';
import { Communications } from './communications/communications';
import { Support } from './support/support';
import { CreateTicket } from './support/create-ticket/create-ticket';
import { Notifications } from './notifications/notifications';
import { Cart } from './cart/cart';
import { Reception } from './reception/reception';

const routes: Routes = [
  {
    path: 'auth',
    children: [
      { path: 'registration', component: Registration },
      { path: 'rejection', component: Rejection },
      { path: '', redirectTo: 'registration', pathMatch: 'full' }
    ]
  },
  {
    path: '',
    component: Layout,
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'catalog', component: Catalog },
      { path: 'cart', component: Cart },
      { path: 'suppliers', component: Suppliers },
      { path: 'suppliers/:id/catalog', component: SupplierCatalog },
      { path: 'orders', component: Orders },
      { path: 'orders/:id', component: OrderDetail },
      { path: 'reception', component: Reception },
      { path: 'analytics', component: Analytics },
      { path: 'communications', component: Communications },
      { path: 'support', component: Support },
      { path: 'support/create', component: CreateTicket },
      { path: 'notifications', component: Notifications },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RetailNetworkRoutingModule { }
