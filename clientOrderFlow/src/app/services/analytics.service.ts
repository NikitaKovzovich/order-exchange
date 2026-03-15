import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  AnalyticsPeriod,
  CustomerDashboard,
  ProductPurchaseHistoryItem,
  SupplierDashboard
} from '../models/api.models';

export interface SupplierKPI {
  revenue: number;
  orderCount: number;
  averageCheck: number;
  shippedUnits: number;
  period: string;
}

export interface OrderFunnel {
  pendingConfirmation: number;
  confirmed: number;
  rejected: number;
  awaitingPayment: number;
  pendingPaymentVerification: number;
  paid: number;
  paymentProblem: number;
  awaitingShipment: number;
  shipped: number;
  delivered: number;
  closed: number;
}

export interface SalesDynamicsItem {
  date: string;
  revenue: number;
  orderCount: number;
}

export interface ProductAnalyticsItem {
  productId: number;
  productName: string;
  productSku: string;
  revenue: number;
  quantity: number;
}

export interface ABCAnalysisItem {
  productId: number;
  productName: string;
  category: string;
  revenue: number;
  revenuePercent: number;
  cumulativePercent: number;
}

export interface CustomerAnalyticsItem {
  customerId: number;
  customerName: string;
  orderCount: number;
  totalRevenue: number;
  averageCheck: number;
  lastOrderDate: string;
}

export interface SupplierAnalyticsResponse {
  kpi: SupplierKPI;
  funnel: OrderFunnel;
  salesDynamics: SalesDynamicsItem[];
  productAnalytics: {
    topByRevenue: ProductAnalyticsItem[];
    topByQuantity: ProductAnalyticsItem[];
    abcAnalysis: ABCAnalysisItem[];
    lowStock: any[];
  };
  customerAnalytics: CustomerAnalyticsItem[];
}

export interface CustomerKPI {
  totalExpenses: number;
  orderCount: number;
  supplierCount: number;
  averageCheck: number;
  period: string;
}

export interface ExpensesDynamicsItem {
  date: string;
  expenses: number;
  orderCount: number;
}

export interface ExpenseStructureItem {
  categoryName?: string;
  supplierId?: number;
  supplierName?: string;
  amount: number;
  percent: number;
}

export interface SupplierAnalyticsItem {
  supplierId: number;
  supplierName: string;
  orderCount: number;
  totalAmount: number;
  averageCheck: number;
  lastOrderDate: string;
}

export interface ProductPurchaseRecord {
  date: string;
  supplierId: number;
  supplierName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export interface ProductPurchaseHistory {
  productId: number;
  productName: string;
  productSku: string;
  purchases: ProductPurchaseRecord[];
}

export interface CustomerAnalyticsResponse {
  kpi: CustomerKPI;
  expensesDynamics: ExpensesDynamicsItem[];
  expenseStructure: {
    byCategory: ExpenseStructureItem[];
    bySupplier: ExpenseStructureItem[];
  };
  supplierAnalytics: SupplierAnalyticsItem[];
  productHistory: ProductPurchaseHistory[];
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private readonly API_URL = '/api/analytics';

  constructor(private http: HttpClient) {}

  getSupplierAnalytics(period: AnalyticsPeriod = 'month'): Observable<SupplierAnalyticsResponse> {
    const params = new HttpParams().set('period', period);
    return this.http.get<ApiResponse<SupplierAnalyticsResponse>>(`${this.API_URL}/supplier`, { params }).pipe(
      map(response => response.data!)
    );
  }

  getCustomerAnalytics(period: AnalyticsPeriod = 'month'): Observable<CustomerAnalyticsResponse> {
    const params = new HttpParams().set('period', period);
    return this.http.get<ApiResponse<CustomerAnalyticsResponse>>(`${this.API_URL}/customer`, { params }).pipe(
      map(response => response.data!)
    );
  }

  getSupplierDashboard(): Observable<SupplierDashboard> {
    return this.http.get<ApiResponse<SupplierDashboard>>(`${this.API_URL}/dashboard/supplier`).pipe(
      map(response => response.data!)
    );
  }

  getCustomerDashboard(): Observable<CustomerDashboard> {
    return this.http.get<ApiResponse<CustomerDashboard>>(`${this.API_URL}/dashboard/customer`).pipe(
      map(response => response.data!)
    );
  }

  getCustomerProductHistory(productId?: number): Observable<ProductPurchaseHistoryItem[]> {
    let params = new HttpParams();
    if (productId !== undefined) {
      params = params.set('productId', String(productId));
    }

    return this.http.get<ApiResponse<ProductPurchaseHistoryItem[]>>(`${this.API_URL}/customer/product-history`, { params }).pipe(
      map(response => response.data || [])
    );
  }
}

