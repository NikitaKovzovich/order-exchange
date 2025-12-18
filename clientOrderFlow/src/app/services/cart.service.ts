import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  ApiResponse,
  Cart,
  AddToCartRequest,
  UpdateCartItemRequest,
  CheckoutRequest,
  CheckoutResponse
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly API_URL = '/api/cart';
  private cartSubject = new BehaviorSubject<Cart | null>(null);

  public cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) {}

  getCart(): Observable<Cart> {
    return this.http.get<ApiResponse<Cart>>(this.API_URL).pipe(
      map(response => response.data!),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  addItem(request: AddToCartRequest): Observable<Cart> {
    return this.http.post<ApiResponse<Cart>>(`${this.API_URL}/items`, request).pipe(
      map(response => response.data!),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  updateItem(productId: number, request: UpdateCartItemRequest): Observable<Cart> {
    return this.http.put<ApiResponse<Cart>>(`${this.API_URL}/items/${productId}`, request).pipe(
      map(response => response.data!),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  removeItem(productId: number): Observable<Cart> {
    return this.http.delete<ApiResponse<Cart>>(`${this.API_URL}/items/${productId}`).pipe(
      map(response => response.data!),
      tap(cart => this.cartSubject.next(cart))
    );
  }

  clearCart(): Observable<void> {
    return this.http.delete<ApiResponse<void>>(this.API_URL).pipe(
      map(() => undefined),
      tap(() => this.cartSubject.next(null))
    );
  }

  checkout(request: CheckoutRequest): Observable<CheckoutResponse> {
    return this.http.post<ApiResponse<CheckoutResponse>>(`${this.API_URL}/checkout`, request).pipe(
      map(response => response.data!),
      tap(() => this.cartSubject.next(null))
    );
  }

  getItemCount(): number {
    return this.cartSubject.value?.itemCount || 0;
  }
}

