import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  OrderNotification,
  PageResponse,
  UnreadCountResponse
} from '../models/api.models';
import { ApiClientService } from './api-client.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly API_URL = 'orders/notifications';

  constructor(private apiClient: ApiClientService) {}

  getNotifications(page: number = 0, size: number = 20, unreadOnly?: boolean): Observable<PageResponse<OrderNotification>> {
    const params: Record<string, string> = {
      page: String(page),
      size: String(size)
    };

    if (unreadOnly !== undefined) {
      params['unreadOnly'] = String(unreadOnly);
    }

    return this.apiClient.get<ApiResponse<PageResponse<OrderNotification>>>(this.API_URL, { params }).pipe(
      map(response => response.data || { content: [], totalElements: 0, totalPages: 0, size, number: page })
    );
  }

  getUnreadCount(): Observable<number> {
    return this.apiClient.get<ApiResponse<UnreadCountResponse>>(`${this.API_URL}/unread-count`).pipe(
      map(response => response.data?.unreadCount ?? 0)
    );
  }

  markAsRead(id: number): Observable<OrderNotification> {
    return this.apiClient.post<ApiResponse<OrderNotification>>(`${this.API_URL}/${id}/read`, {}).pipe(
      map(response => response.data!)
    );
  }

  markAllAsRead(): Observable<string | undefined> {
    return this.apiClient.post<ApiResponse<void>>(`${this.API_URL}/read-all`, {}).pipe(
      map(response => response.message)
    );
  }
}

