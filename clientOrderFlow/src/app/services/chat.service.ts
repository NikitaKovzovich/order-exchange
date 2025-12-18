import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  PageResponse,
  ChatChannel,
  ChatMessage,
  CreateChatChannelRequest,
  SendMessageRequest,
  SupportTicket,
  CreateTicketRequest,
  TicketMessage,
  SendTicketMessageRequest
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  createChannel(request: CreateChatChannelRequest): Observable<ChatChannel> {
    return this.http.post<ApiResponse<ChatChannel>>(`${this.API_URL}/chats`, request).pipe(
      map(response => response.data!)
    );
  }

  getMyChannels(): Observable<ChatChannel[]> {
    return this.http.get<ApiResponse<ChatChannel[]>>(`${this.API_URL}/chats`).pipe(
      map(response => response.data || [])
    );
  }

  getChannelByOrder(orderId: number): Observable<ChatChannel> {
    return this.http.get<ApiResponse<ChatChannel>>(`${this.API_URL}/chats/order/${orderId}`).pipe(
      map(response => response.data!)
    );
  }

  getMessages(orderId: number, page: number = 0, size: number = 50): Observable<PageResponse<ChatMessage>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<ChatMessage>>>(`${this.API_URL}/chats/order/${orderId}/messages`, { params }).pipe(
      map(response => response.data!)
    );
  }

  sendMessage(orderId: number, request: SendMessageRequest): Observable<ChatMessage> {
    return this.http.post<ApiResponse<ChatMessage>>(`${this.API_URL}/chats/order/${orderId}/messages`, request).pipe(
      map(response => response.data!)
    );
  }

  markAsRead(orderId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/chats/order/${orderId}/read`, {});
  }

  deactivateChannel(orderId: number): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/chats/order/${orderId}/deactivate`, {});
  }

  createTicket(request: CreateTicketRequest): Observable<SupportTicket> {
    return this.http.post<ApiResponse<SupportTicket>>(`${this.API_URL}/support/tickets`, request).pipe(
      map(response => response.data!)
    );
  }

  getMyTickets(): Observable<SupportTicket[]> {
    return this.http.get<ApiResponse<SupportTicket[]>>(`${this.API_URL}/support/tickets`).pipe(
      map(response => response.data || [])
    );
  }

  getTicketById(ticketId: number): Observable<SupportTicket> {
    return this.http.get<ApiResponse<SupportTicket>>(`${this.API_URL}/support/tickets/${ticketId}`).pipe(
      map(response => response.data!)
    );
  }

  getTicketMessages(ticketId: number): Observable<TicketMessage[]> {
    return this.http.get<ApiResponse<TicketMessage[]>>(`${this.API_URL}/support/tickets/${ticketId}/messages`).pipe(
      map(response => response.data || [])
    );
  }

  sendTicketMessage(ticketId: number, request: SendTicketMessageRequest): Observable<TicketMessage> {
    return this.http.post<ApiResponse<TicketMessage>>(`${this.API_URL}/support/tickets/${ticketId}/messages`, request).pipe(
      map(response => response.data!)
    );
  }

  closeTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<SupportTicket>>(`${this.API_URL}/support/tickets/${ticketId}/close`, {}).pipe(
      map(response => response.data!)
    );
  }

  getAllTickets(): Observable<SupportTicket[]> {
    return this.http.get<ApiResponse<SupportTicket[]>>(`${this.API_URL}/support/tickets/admin`).pipe(
      map(response => response.data || [])
    );
  }

  assignTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<SupportTicket>>(`${this.API_URL}/support/tickets/${ticketId}/assign`, {}).pipe(
      map(response => response.data!)
    );
  }
}

