import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  CreateTicketRequest,
  PageResponse,
  SendTicketMessageRequest,
  SupportTicket,
  TicketMessage,
  TicketStatus
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class SupportService {
  private readonly API_URL = '/api/support/tickets';

  constructor(private http: HttpClient) {}

  getUserTickets(page: number = 0, size: number = 20): Observable<PageResponse<SupportTicket>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));

    return this.http.get<ApiResponse<PageResponse<unknown>>>(this.API_URL, { params }).pipe(
      map(response => this.mapPageResponse(response.data, page, size))
    );
  }

  createTicket(request: CreateTicketRequest): Observable<SupportTicket> {
    return this.http.post<ApiResponse<unknown>>(this.API_URL, request).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  createTicketMultipart(
    subject: string,
    message: string,
    category?: string,
    priority?: string,
    files: File[] = []
  ): Observable<SupportTicket> {
    const formData = new FormData();
    formData.append('subject', subject);
    formData.append('message', message);

    if (category) {
      formData.append('category', category);
    }
    if (priority) {
      formData.append('priority', priority);
    }
    for (const file of files) {
      formData.append('files', file, file.name);
    }

    return this.http.post<ApiResponse<unknown>>(this.API_URL, formData).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  getTicketById(ticketId: number): Observable<SupportTicket> {
    return this.http.get<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}`).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  getTicketMessages(ticketId: number): Observable<TicketMessage[]> {
    return this.http.get<ApiResponse<unknown[]>>(`${this.API_URL}/${ticketId}/messages`).pipe(
      map(response => (response.data || []).map(message => this.mapMessage(message)))
    );
  }

  sendTicketMessage(ticketId: number, request: SendTicketMessageRequest): Observable<TicketMessage> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/messages`, request).pipe(
      map(response => this.mapMessage(response.data))
    );
  }

  sendTicketMessageMultipart(
    ticketId: number,
    message: string,
    files: File[] = [],
    isInternalNote: boolean = false
  ): Observable<TicketMessage> {
    const formData = new FormData();
    formData.append('message', message);
    formData.append('isInternalNote', String(isInternalNote));

    for (const file of files) {
      formData.append('files', file, file.name);
    }

    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/messages`, formData).pipe(
      map(response => this.mapMessage(response.data))
    );
  }

  getAdminTickets(status?: TicketStatus | string, search?: string, page: number = 0, size: number = 20): Observable<PageResponse<SupportTicket>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));

    if (status) {
      params = params.set('status', status);
    }

    if (search) {
      params = params.set('search', search);
    }

    return this.http.get<ApiResponse<PageResponse<unknown>>>(`${this.API_URL}/admin`, { params }).pipe(
      map(response => this.mapPageResponse(response.data, page, size))
    );
  }

  assignTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/assign`, {}).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  resolveTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/resolve`, {}).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  closeTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/close`, {}).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  reopenTicket(ticketId: number): Observable<SupportTicket> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/${ticketId}/reopen`, {}).pipe(
      map(response => this.mapTicket(response.data))
    );
  }

  private mapPageResponse(data: PageResponse<unknown> | undefined, page: number, size: number): PageResponse<SupportTicket> {
    return {
      content: (data?.content || []).map(ticket => this.mapTicket(ticket)),
      totalElements: data?.totalElements || 0,
      totalPages: data?.totalPages || 0,
      size: data?.size || size,
      number: data?.number || page,
      first: data?.first ?? page === 0,
      last: data?.last ?? (data?.totalPages || 0) <= page + 1
    };
  }

  private mapTicket(item: unknown): SupportTicket {
    const raw = (item || {}) as Record<string, unknown>;
    const rawLastMessage = (raw['lastMessage'] || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] || 0),
      ticketNumber: `TKT-${raw['id'] || '0'}`,
      companyId: Number(raw['companyId'] || raw['requesterCompanyId'] || 0),
      userId: Number(raw['userId'] || raw['requesterUserId'] || 0),
      userEmail: raw['userEmail'] ? String(raw['userEmail']) : undefined,
      subject: String(raw['subject'] || ''),
      category: String(raw['category'] || 'OTHER') as SupportTicket['category'],
      priority: this.mapPriority(raw['priority']),
      status: this.mapStatus(raw['status']),
      assignedTo: raw['assignedTo'] ? Number(raw['assignedTo']) : undefined,
      assignedAdminId: raw['assignedAdminId'] ? Number(raw['assignedAdminId']) : undefined,
      assignedAdminEmail: raw['assignedAdminEmail'] ? String(raw['assignedAdminEmail']) : undefined,
      createdAt: String(raw['createdAt'] || ''),
      updatedAt: String(raw['updatedAt'] || raw['createdAt'] || ''),
      resolvedAt: raw['resolvedAt'] ? String(raw['resolvedAt']) : undefined,
      closedAt: raw['closedAt'] ? String(raw['closedAt']) : undefined,
      lastMessageAt: rawLastMessage['createdAt'] ? String(rawLastMessage['createdAt']) : rawLastMessage['sentAt'] ? String(rawLastMessage['sentAt']) : undefined,
      lastMessage: rawLastMessage['messageText'] ? String(rawLastMessage['messageText']) : undefined,
      messageCount: Number(raw['messageCount'] || 0)
    };
  }

  private mapMessage(item: unknown): TicketMessage {
    const raw = (item || {}) as Record<string, unknown>;
    const isAdminReply = Boolean(raw['isAdminReply']);

    return {
      id: Number(raw['id'] || 0),
      ticketId: Number(raw['ticketId'] || 0),
      senderId: Number(raw['senderId'] || 0),
      senderEmail: isAdminReply ? 'admin@orderflow.local' : 'user@orderflow.local',
      senderRole: isAdminReply ? 'ADMIN' : 'USER',
      messageText: String(raw['messageText'] || ''),
      attachmentKeys: Array.isArray(raw['attachmentKeys']) ? raw['attachmentKeys'].map(value => String(value)) : [],
      attachmentKey: raw['attachmentKey'] ? String(raw['attachmentKey']) : undefined,
      isInternalNote: raw['isInternalNote'] !== undefined ? Boolean(raw['isInternalNote']) : undefined,
      createdAt: String(raw['createdAt'] || raw['sentAt'] || '')
    };
  }

  private mapStatus(status: unknown): TicketStatus {
    switch (String(status || '')) {
      case 'NEW':
        return 'OPEN';
      case 'WAITING_FOR_CUSTOMER':
        return 'WAITING_USER';
      case 'IN_PROGRESS':
        return 'IN_PROGRESS';
      case 'RESOLVED':
        return 'RESOLVED';
      case 'CLOSED':
        return 'CLOSED';
      default:
        return 'OPEN';
    }
  }

  private mapPriority(priority: unknown): SupportTicket['priority'] {
    switch (String(priority || '')) {
      case 'HIGH':
      case 'URGENT':
        return 'HIGH';
      case 'LOW':
        return 'LOW';
      default:
        return 'NORMAL';
    }
  }
}

