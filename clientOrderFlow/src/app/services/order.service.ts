import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  PageResponse,
  Order,
  OrderSummary,
  OrderStatus,
  RejectOrderRequest,
  PaymentProofRequest,
  CreateDiscrepancyRequest,
  DiscrepancyReport
} from '../models/api.models';

export interface CreateOrderRequest {
  supplierId: number;
  deliveryAddress: string;
  desiredDeliveryDate: string;
  items: {
    productId: number;
    quantity: number;
    unitPrice: number;
    vatRate: number;
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  createOrder(request: CreateOrderRequest): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders`, request).pipe(
      map(response => response.data!)
    );
  }

  getOrderById(id: number): Observable<Order> {
    return this.http.get<ApiResponse<Order>>(`${this.API_URL}/orders/${id}`).pipe(
      map(response => response.data!)
    );
  }

  getOrderByNumber(orderNumber: string): Observable<Order> {
    return this.http.get<ApiResponse<Order>>(`${this.API_URL}/orders/number/${orderNumber}`).pipe(
      map(response => response.data!)
    );
  }

  getSupplierOrders(status?: OrderStatus, page: number = 0, size: number = 20): Observable<PageResponse<OrderSummary>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);

    return this.http.get<ApiResponse<PageResponse<OrderSummary>>>(`${this.API_URL}/orders/supplier`, { params }).pipe(
      map(response => response.data!)
    );
  }

  getCustomerOrders(status?: OrderStatus, page: number = 0, size: number = 20): Observable<PageResponse<OrderSummary>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (status) params = params.set('status', status);

    return this.http.get<ApiResponse<PageResponse<OrderSummary>>>(`${this.API_URL}/orders/customer`, { params }).pipe(
      map(response => response.data!)
    );
  }

  confirmOrder(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/confirm`, {}).pipe(
      map(response => response.data!)
    );
  }

  rejectOrder(orderId: number, request: RejectOrderRequest): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/reject`, request).pipe(
      map(response => response.data!)
    );
  }

  uploadPaymentProof(orderId: number, request: PaymentProofRequest): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/payment-proof`, request).pipe(
      map(response => response.data!)
    );
  }

  confirmPayment(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/confirm-payment`, {}).pipe(
      map(response => response.data!)
    );
  }

  rejectPayment(orderId: number, reason: string): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/reject-payment`, { reason }).pipe(
      map(response => response.data!)
    );
  }

  shipOrder(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/ship`, {}).pipe(
      map(response => response.data!)
    );
  }

  confirmDelivery(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/deliver`, {}).pipe(
      map(response => response.data!)
    );
  }

  closeOrder(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/close`, {}).pipe(
      map(response => response.data!)
    );
  }

  cancelOrder(orderId: number): Observable<Order> {
    return this.http.post<ApiResponse<Order>>(`${this.API_URL}/orders/${orderId}/cancel`, {}).pipe(
      map(response => response.data!)
    );
  }

  createDiscrepancy(orderId: number, request: CreateDiscrepancyRequest): Observable<DiscrepancyReport> {
    return this.http.post<ApiResponse<DiscrepancyReport>>(`${this.API_URL}/orders/${orderId}/discrepancy`, request).pipe(
      map(response => response.data!)
    );
  }

  getOrderDiscrepancies(orderId: number): Observable<DiscrepancyReport[]> {
    return this.http.get<ApiResponse<DiscrepancyReport[]>>(`${this.API_URL}/orders/${orderId}/discrepancies`).pipe(
      map(response => response.data || [])
    );
  }
}

