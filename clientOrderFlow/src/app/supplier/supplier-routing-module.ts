import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Registration } from './auth/registration/registration';
import { Rejection } from './auth/rejection/rejection';
import { Layout } from './shared/layout/layout';
import { Dashboard } from './dashboard/dashboard';
import { Catalog } from './catalog/catalog';
import { Orders } from './orders/orders';
import { OrderDetail } from './orders/order-detail/order-detail';
import { Invoice } from './orders/invoice/invoice';
import { UPD } from './orders/upd/upd';
import { Analytics } from './analytics/analytics';
import { Communications } from './communications/communications';
import { Support } from './support/support';
import { CreateTicket } from './support/create-ticket/create-ticket';
import { Notifications } from './notifications/notifications';
import { ProductCard } from './catalog/product-card/product-card';
import { AddProduct } from './catalog/add-product/add-product';
import { EmptyCatalog } from './catalog/empty-catalog/empty-catalog';
import { authGuard } from '../guards/auth.guard';

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
    canActivate: [authGuard],
    data: { roles: ['SUPPLIER'] },
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'catalog', component: Catalog },
      { path: 'catalog/empty', component: EmptyCatalog },
      { path: 'catalog/add', component: AddProduct },
      { path: 'catalog/:id', component: ProductCard },
      { path: 'orders', component: Orders },
      { path: 'orders/:id', component: OrderDetail },
      { path: 'orders/:id/invoice', component: Invoice },
      { path: 'orders/:id/upd', component: UPD },
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
export class SupplierRoutingModule { }
