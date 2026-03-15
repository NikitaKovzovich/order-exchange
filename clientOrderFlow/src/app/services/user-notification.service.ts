import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PageResponse, UnreadCountResponse, UserNotification } from '../models/api.models';

interface RawUserNotificationPage {
  content?: unknown[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
}

@Injectable({
  providedIn: 'root'
})
export class UserNotificationService {
  private readonly API_URL = '/api/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(page: number = 0, size: number = 20): Observable<PageResponse<UserNotification>> {
    return this.http.get<RawUserNotificationPage>(`${this.API_URL}?page=${page}&size=${size}`).pipe(
      map(response => ({
        content: (response.content || []).map(item => this.mapNotification(item)),
        totalElements: Number(response.totalElements ?? 0),
        totalPages: Number(response.totalPages ?? 0),
        size: Number(response.size ?? size),
        number: Number(response.page ?? page),
        first: Number(response.page ?? page) === 0,
        last: Number(response.page ?? page) + 1 >= Number(response.totalPages ?? 0)
      }))
    );
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<UnreadCountResponse>(`${this.API_URL}/unread-count`).pipe(
      map(response => Number(response.unreadCount ?? 0))
    );
  }

  markAsRead(id: number): Observable<string | undefined> {
    return this.http.post<{ message?: string }>(`${this.API_URL}/${id}/read`, {}).pipe(
      map(response => response.message)
    );
  }

  markAllAsRead(): Observable<string | undefined> {
    return this.http.post<{ message?: string; updated?: number }>(`${this.API_URL}/read-all`, {}).pipe(
      map(response => response.message)
    );
  }

  private mapNotification(item: unknown): UserNotification {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      title: String(raw['title'] ?? 'Уведомление'),
      message: String(raw['message'] ?? ''),
      type: String(raw['type'] ?? 'SYSTEM'),
      isRead: Boolean(raw['isRead'] ?? false),
      createdAt: String(raw['createdAt'] ?? ''),
      relatedEntityType: raw['relatedEntityType'] ? String(raw['relatedEntityType']) : null,
      relatedEntityId: raw['relatedEntityId'] !== undefined && raw['relatedEntityId'] !== null
        ? Number(raw['relatedEntityId'])
        : null
    };
  }
}

