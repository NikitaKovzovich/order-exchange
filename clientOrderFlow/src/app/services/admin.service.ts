import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ApiResponse, CompanyAddress, CompanyDocumentInfo, CompanyProfile, PageResponse, Product } from '../models/api.models';

export interface User {
  id: number;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
  status: string;
  isActive: boolean;
  company: {
    id: number;
    legalName: string;
    name?: string;
    legalForm?: string;
    taxId?: string;
    status?: string;
    verified?: boolean;
  };
  createdAt: string;
}

export interface AdminUserOrderStats {
  totalOrders: number;
  totalRevenue: number;
  lastOrderDate: string | null;
  [key: string]: number | string | null;
}

export interface AdminUserDetail extends User {
  companyProfile?: CompanyProfile;
  orderStats?: AdminUserOrderStats;
}

export interface AdminUserUpdatePayload {
  email?: string;
  contactPhone?: string;
  name?: string;
  bankName?: string;
  bic?: string;
  accountNumber?: string;
  directorName?: string;
  chiefAccountantName?: string;
  paymentTerms?: string;
}

export interface VerificationRequest {
  id: number;
  companyId: number;
  companyName: string;
  taxId: string;
  role?: 'SUPPLIER' | 'RETAIL_CHAIN' | 'ADMIN' | string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  submittedAt: string;
  documents?: VerificationDocument[];
  rejectionReason?: string | null;
}

export interface VerificationDocument {
  id: number;
  documentType: string;
  documentName: string;
  documentPath: string;
  originalFilename?: string;
  uploadedAt?: string;
  downloadUrl?: string;
  isImage?: boolean;
}

export interface VerificationDetails extends VerificationRequest {
  reviewedAt?: string | null;
  role?: 'SUPPLIER' | 'RETAIL_CHAIN' | 'ADMIN' | string;
  company: {
    id: number;
    companyName?: string;
    name?: string;
    legalName: string;
    legalForm: string;
    legalFormText?: string;
    taxId: string;
    inn?: string;
    registrationDate: string;
    contactPhone: string;
    contactEmail?: string;
    status: string;
    verified: boolean;
  };
  addresses?: Record<string, string>;
  requisites?: {
    directorName?: string;
    chiefAccountantName?: string;
    bankName?: string;
    bic?: string;
    accountNumber?: string;
  };
  paymentTerms?: string;
  user?: {
    id: number;
    email: string;
    role: string;
  };
}

export interface PendingVerificationRequest {
  id: number;
  company: {
    id: number;
    legalName: string;
    taxId: string;
  };
  user: {
    id: number;
    email: string;
    role: string;
  };
  status: 'PENDING';
  createdAt: string;
  requestedAt: string;
  documents: VerificationDocument[];
}

export interface AdminOrdersStats {
  [key: string]: number | string;
}

export interface VerificationRateStats {
  [key: string]: number | string;
}

export interface RecentRegistrationItem {
  id: number;
  companyName: string;
  role: string;
  date: string;
}

export interface RecentSupportTicketItem {
  id?: number;
  ticketId?: number;
  ticketNumber?: string;
  subject?: string;
  userEmail?: string;
  status?: string;
  createdAt?: string;
}

export interface RegistrationActivityPoint {
  date: string;
  count: number;
}

export interface UserEvent {
  id?: number;
  eventType?: string;
  description?: string;
  aggregateType?: string;
  aggregateId?: string;
  version?: number;
  payload?: unknown;
  createdAt: string;
}

export interface AdminUsersFilters {
  role?: string;
  status?: string;
  search?: string;
  page?: number;
  size?: number;
}

export interface AdminVerificationFilters {
  status?: 'PENDING' | 'APPROVED' | 'REJECTED';
  role?: string;
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly API_URL = '/api/admin';
  private readonly VERIFICATION_URL = '/api/admin/verification';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.searchUsers({ page: 0, size: 1000 }).pipe(
      map(response => response.content),
      catchError(error => {
        console.error('Get users error:', error);
        return throwError(() => error);
      })
    );
  }

  searchUsers(filters: AdminUsersFilters = {}): Observable<PageResponse<User>> {
    const searchParams = new URLSearchParams();
    if (filters.role) searchParams.set('role', filters.role);
    if (filters.status) searchParams.set('status', filters.status);
    if (filters.search) searchParams.set('search', filters.search);
    if (filters.page !== undefined) searchParams.set('page', String(filters.page));
    if (filters.size !== undefined) searchParams.set('size', String(filters.size));

    const query = searchParams.toString();
    const url = query ? `${this.API_URL}/users?${query}` : `${this.API_URL}/users`;

    return this.http.get<Record<string, unknown>>(url).pipe(
      map(response => this.normalizePage<User>(response, item => this.mapUser(item))),
      catchError(error => {
        console.error('Search users error:', error);
        return throwError(() => error);
      })
    );
  }

  getUserById(userId: number): Observable<AdminUserDetail> {
    return this.http.get<Record<string, unknown>>(`${this.API_URL}/users/${userId}`).pipe(
      map(response => this.mapUserDetail(response)),
      catchError(error => {
        console.error('Get user error:', error);
        return throwError(() => error);
      })
    );
  }

  blockUser(userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/users/${userId}/block`, {}).pipe(
      catchError(error => {
        console.error('Block user error:', error);
        return throwError(() => error);
      })
    );
  }

  unblockUser(userId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/users/${userId}/unblock`, {}).pipe(
      catchError(error => {
        console.error('Unblock user error:', error);
        return throwError(() => error);
      })
    );
  }

  updateUser(userId: number, payload: AdminUserUpdatePayload): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${this.API_URL}/users/${userId}`, payload).pipe(
      catchError(error => {
        console.error('Update user error:', error);
        return throwError(() => error);
      })
    );
  }

  deleteUser(userId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/users/${userId}`).pipe(
      catchError(error => {
        console.error('Delete user error:', error);
        return throwError(() => error);
      })
    );
  }

  getUserEvents(userId: number, page: number = 0, size: number = 20): Observable<PageResponse<UserEvent>> {
    return this.http.get<Record<string, unknown>>(`${this.API_URL}/users/${userId}/events?page=${page}&size=${size}`).pipe(
      map(response => this.normalizePage<UserEvent>(response, item => this.mapUserEvent(item))),
      catchError(error => {
        console.error('Get user events error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationRequests(status?: 'PENDING' | 'APPROVED' | 'REJECTED'): Observable<VerificationRequest[]> {
    return this.searchVerificationRequests({ status, page: 0, size: 1000 }).pipe(
      map(response => response.content),
      catchError(error => {
        console.error('Get verification requests error:', error);
        return throwError(() => error);
      })
    );
  }

  searchVerificationRequests(filters: AdminVerificationFilters = {}): Observable<PageResponse<VerificationRequest>> {
    const searchParams = new URLSearchParams();
    if (filters.status) searchParams.set('status', filters.status);
    if (filters.role) searchParams.set('role', filters.role);
    if (filters.search) searchParams.set('search', filters.search);
    if (filters.page !== undefined) searchParams.set('page', String(filters.page));
    if (filters.size !== undefined) searchParams.set('size', String(filters.size));

    const query = searchParams.toString();
    const url = query ? `${this.VERIFICATION_URL}?${query}` : this.VERIFICATION_URL;

    return this.http.get<{ data?: Record<string, unknown> }>(url).pipe(
      map(response => this.normalizePage<VerificationRequest>(response.data || {}, item => this.mapVerificationRequest(item))),
      catchError(error => {
        console.error('Search verification requests error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationById(verificationId: number): Observable<VerificationDetails> {
    return this.http.get<Record<string, unknown>>(`${this.VERIFICATION_URL}/${verificationId}`).pipe(
      map(response => this.mapVerificationDetails(response)),
      catchError(error => {
        console.error('Get verification details error:', error);
        return throwError(() => error);
      })
    );
  }

  approveVerification(verificationId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.VERIFICATION_URL}/${verificationId}/approve`, {}).pipe(
      catchError(error => {
        console.error('Approve verification error:', error);
        return throwError(() => error);
      })
    );
  }

  rejectVerification(verificationId: number, reason: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.VERIFICATION_URL}/${verificationId}/reject`,
      { reason }
    ).pipe(
      catchError(error => {
        console.error('Reject verification error:', error);
        return throwError(() => error);
      })
    );
  }

  getPendingVerificationRequests(): Observable<PendingVerificationRequest[]> {
    return this.http.get<PendingVerificationRequest[]>(`${this.VERIFICATION_URL}/pending`).pipe(
      catchError(error => {
        console.error('Get pending verification requests error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationDocuments(verificationId: number): Observable<VerificationDocument[]> {
    return this.http.get<VerificationDocument[]>(`${this.VERIFICATION_URL}/${verificationId}/documents`).pipe(
      catchError(error => {
        console.error('Get verification documents error:', error);
        return throwError(() => error);
      })
    );
  }

  getDashboardStats(): Observable<{ totalUsers: number; usersWeekGrowth: number; pendingVerifications: number }> {
    return this.http.get<{ totalUsers: number; usersWeekGrowth: number; pendingVerifications: number }>(`${this.API_URL}/dashboard/stats`).pipe(
      catchError(error => {
        console.error('Get dashboard stats error:', error);
        return throwError(() => error);
      })
    );
  }

  getUsersStats(): Observable<{ total: number; suppliers: number; retailers: number; admins: number }> {
    return this.http.get<{ total: number; suppliers: number; retailers: number; admins: number }>(`${this.API_URL}/dashboard/users-stats`).pipe(
      catchError(error => {
        console.error('Get users stats error:', error);
        return throwError(() => error);
      })
    );
  }

  getOrdersStats(): Observable<AdminOrdersStats> {
    return this.http.get<AdminOrdersStats>(`${this.API_URL}/dashboard/orders-stats`).pipe(
      catchError(error => {
        console.error('Get orders stats error:', error);
        return throwError(() => error);
      })
    );
  }

  getRecentRegistrations(): Observable<RecentRegistrationItem[]> {
    return this.http.get<RecentRegistrationItem[]>(`${this.API_URL}/dashboard/recent-registrations`).pipe(
      catchError(error => {
        console.error('Get recent registrations error:', error);
        return throwError(() => error);
      })
    );
  }

  getRecentSupportTickets(): Observable<RecentSupportTicketItem[]> {
    return this.http.get<RecentSupportTicketItem[]>(`${this.API_URL}/dashboard/recent-support-tickets`).pipe(
      catchError(error => {
        console.error('Get recent support tickets error:', error);
        return throwError(() => error);
      })
    );
  }

  getRegistrationActivity(days: number = 30): Observable<RegistrationActivityPoint[]> {
    return this.http.get<RegistrationActivityPoint[]>(`${this.API_URL}/dashboard/registration-activity?days=${days}`).pipe(
      catchError(error => {
        console.error('Get registration activity error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationRate(): Observable<VerificationRateStats> {
    return this.http.get<VerificationRateStats>(`${this.API_URL}/dashboard/verification-rate`).pipe(
      catchError(error => {
        console.error('Get verification rate error:', error);
        return throwError(() => error);
      })
    );
  }

  getAdminProducts(supplierId?: number, categoryId?: number, status?: string, search?: string, page: number = 0, size: number = 20): Observable<PageResponse<Product>> {
    const searchParams = new URLSearchParams();
    if (supplierId !== undefined) searchParams.set('supplierId', String(supplierId));
    if (categoryId !== undefined) searchParams.set('categoryId', String(categoryId));
    if (status) searchParams.set('status', status);
    if (search) searchParams.set('search', search);
    searchParams.set('page', String(page));
    searchParams.set('size', String(size));

    return this.http.get<ApiResponse<PageResponse<Product>>>(`/api/products/admin/all?${searchParams.toString()}`).pipe(
      map(response => response.data!),
      catchError(error => {
        console.error('Get admin products error:', error);
        return throwError(() => error);
      })
    );
  }

  hideProduct(productId: number): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`/api/products/${productId}/hide`, {}).pipe(
      map(response => response.data!),
      catchError(error => {
        console.error('Hide product error:', error);
        return throwError(() => error);
      })
    );
  }

  showProduct(productId: number): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`/api/products/${productId}/show`, {}).pipe(
      map(response => response.data!),
      catchError(error => {
        console.error('Show product error:', error);
        return throwError(() => error);
      })
    );
  }

  deleteAdminProduct(productId: number): Observable<void> {
    return this.http.delete<void>(`/api/products/${productId}/admin`).pipe(
      catchError(error => {
        console.error('Delete admin product error:', error);
        return throwError(() => error);
      })
    );
  }

  private normalizePage<T>(response: Record<string, unknown>, mapper: (item: unknown) => T): PageResponse<T> {
    const rawContent = Array.isArray(response['content']) ? response['content'] : [];

    return {
      content: rawContent.map(mapper),
      totalElements: Number(response['totalElements'] ?? 0),
      totalPages: Number(response['totalPages'] ?? 0),
      size: Number(response['size'] ?? rawContent.length ?? 0),
      number: Number(response['number'] ?? response['page'] ?? 0),
      first: Boolean(response['first'] ?? Number(response['number'] ?? response['page'] ?? 0) === 0),
      last: Boolean(response['last'] ?? (Number(response['totalPages'] ?? 0) <= Number(response['number'] ?? response['page'] ?? 0) + 1))
    };
  }

  private mapUser(item: unknown): User {
    const raw = (item || {}) as Record<string, unknown>;
    const rawCompany = (raw['company'] || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      email: String(raw['email'] ?? ''),
      role: String(raw['role'] ?? 'SUPPLIER') as User['role'],
      status: String(raw['status'] ?? 'ACTIVE'),
      isActive: Boolean(raw['isActive'] ?? raw['status'] === 'ACTIVE'),
      createdAt: String(raw['createdAt'] ?? ''),
      company: {
        id: Number(rawCompany['id'] ?? 0),
        legalName: String(rawCompany['legalName'] ?? ''),
        name: rawCompany['name'] ? String(rawCompany['name']) : undefined,
        legalForm: rawCompany['legalForm'] ? String(rawCompany['legalForm']) : undefined,
        taxId: rawCompany['taxId'] ? String(rawCompany['taxId']) : undefined,
        status: rawCompany['status'] ? String(rawCompany['status']) : undefined,
        verified: rawCompany['verified'] !== undefined ? Boolean(rawCompany['verified']) : undefined
      }
    };
  }

  private mapUserDetail(item: unknown): AdminUserDetail {
    const raw = (item || {}) as Record<string, unknown>;
    const baseUser = this.mapUser(raw);
    const rawCompanyProfile = raw['companyProfile'] as Record<string, unknown> | undefined;
    const rawOrderStats = (raw['orderStats'] || {}) as Record<string, unknown>;

    return {
      ...baseUser,
      companyProfile: rawCompanyProfile ? this.mapCompanyProfile(rawCompanyProfile) : undefined,
      orderStats: {
        totalOrders: Number(rawOrderStats['totalOrders'] ?? 0),
        totalRevenue: Number(rawOrderStats['totalRevenue'] ?? 0),
        lastOrderDate: rawOrderStats['lastOrderDate'] ? String(rawOrderStats['lastOrderDate']) : null,
        ...rawOrderStats
      }
    };
  }

  private mapCompanyProfile(item: Record<string, unknown>): CompanyProfile {
    return {
      id: Number(item['id'] ?? 0),
      name: item['name'] ? String(item['name']) : null,
      legalName: String(item['legalName'] ?? ''),
      legalForm: String(item['legalForm'] ?? ''),
      legalFormText: item['legalFormText'] ? String(item['legalFormText']) : null,
      taxId: String(item['taxId'] ?? ''),
      registrationDate: String(item['registrationDate'] ?? ''),
      status: String(item['status'] ?? ''),
      contactPhone: String(item['contactPhone'] ?? ''),
      verified: Boolean(item['verified']),
      addresses: Array.isArray(item['addresses'])
        ? item['addresses'].map(address => this.mapCompanyAddress(address))
        : [],
      bankName: item['bankName'] ? String(item['bankName']) : null,
      bic: item['bic'] ? String(item['bic']) : null,
      accountNumber: item['accountNumber'] ? String(item['accountNumber']) : null,
      directorName: item['directorName'] ? String(item['directorName']) : null,
      chiefAccountantName: item['chiefAccountantName'] ? String(item['chiefAccountantName']) : null,
      documents: Array.isArray(item['documents'])
        ? item['documents'].map(document => this.mapCompanyDocument(document))
        : [],
      paymentTerms: item['paymentTerms'] ? String(item['paymentTerms']) : null
    };
  }

  private mapCompanyAddress(item: unknown): CompanyAddress {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      addressType: String(raw['addressType'] ?? raw['type'] ?? ''),
      fullAddress: String(raw['fullAddress'] ?? ''),
      isDefault: Boolean(raw['isDefault'])
    };
  }

  private mapCompanyDocument(item: unknown): CompanyDocumentInfo {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      documentType: String(raw['documentType'] ?? raw['type'] ?? ''),
      originalFilename: String(raw['originalFilename'] ?? ''),
      downloadUrl: String(raw['downloadUrl'] ?? '')
    };
  }

  private mapUserEvent(item: unknown): UserEvent {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: raw['id'] ? Number(raw['id']) : undefined,
      eventType: raw['eventType'] ? String(raw['eventType']) : undefined,
      description: raw['payload'] ? JSON.stringify(raw['payload']) : undefined,
      aggregateType: raw['aggregateType'] ? String(raw['aggregateType']) : undefined,
      aggregateId: raw['aggregateId'] ? String(raw['aggregateId']) : undefined,
      version: raw['version'] ? Number(raw['version']) : undefined,
      payload: raw['payload'],
      createdAt: String(raw['createdAt'] ?? '')
    };
  }

  private mapVerificationRequest(item: unknown): VerificationRequest {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      companyId: Number(raw['companyId'] ?? 0),
      companyName: String(raw['companyName'] ?? ''),
      taxId: String(raw['taxId'] ?? ''),
      role: raw['role'] ? String(raw['role']) as VerificationRequest['role'] : undefined,
      status: String(raw['status'] ?? 'PENDING') as VerificationRequest['status'],
      submittedAt: String(raw['submittedAt'] ?? ''),
      rejectionReason: raw['rejectionReason'] ? String(raw['rejectionReason']) : null,
      documents: []
    };
  }

  private mapVerificationDetails(item: unknown): VerificationDetails {
    const raw = (item || {}) as Record<string, unknown>;
    const rawCompany = (raw['company'] || {}) as Record<string, unknown>;
    const rawDocuments = Array.isArray(raw['documents']) ? raw['documents'] : [];

    return {
      id: Number(raw['id'] ?? 0),
      companyId: Number(rawCompany['id'] ?? 0),
      companyName: String(rawCompany['legalName'] ?? rawCompany['name'] ?? ''),
      taxId: String(rawCompany['taxId'] ?? ''),
      role: raw['role'] ? String(raw['role']) as VerificationDetails['role'] : undefined,
      status: String(raw['status'] ?? 'PENDING') as VerificationDetails['status'],
      submittedAt: String(raw['submittedAt'] ?? ''),
      reviewedAt: raw['reviewedAt'] ? String(raw['reviewedAt']) : null,
      rejectionReason: raw['rejectionReason'] ? String(raw['rejectionReason']) : null,
      documents: rawDocuments.map(document => this.mapVerificationDocument(document)),
      company: {
        id: Number(rawCompany['id'] ?? 0),
        companyName: String(rawCompany['legalName'] ?? rawCompany['name'] ?? ''),
        name: rawCompany['name'] ? String(rawCompany['name']) : undefined,
        legalName: String(rawCompany['legalName'] ?? ''),
        legalForm: String(rawCompany['legalForm'] ?? ''),
        legalFormText: rawCompany['legalFormText'] ? String(rawCompany['legalFormText']) : undefined,
        taxId: String(rawCompany['taxId'] ?? ''),
        inn: String(rawCompany['taxId'] ?? ''),
        registrationDate: String(rawCompany['registrationDate'] ?? ''),
        contactPhone: String(rawCompany['contactPhone'] ?? ''),
        contactEmail: rawCompany['contactEmail'] ? String(rawCompany['contactEmail']) : undefined,
        status: String(rawCompany['status'] ?? ''),
        verified: String(raw['status'] ?? '') === 'APPROVED'
      },
      addresses: (raw['addresses'] || {}) as Record<string, string>,
      requisites: (raw['requisites'] || {}) as VerificationDetails['requisites'],
      paymentTerms: raw['paymentTerms'] ? String(raw['paymentTerms']) : undefined,
      user: rawCompany['contactEmail']
        ? {
            id: 0,
            email: String(rawCompany['contactEmail']),
            role: String(raw['role'] ?? '')
          }
        : undefined
    };
  }

  private mapVerificationDocument(item: unknown): VerificationDocument {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      documentType: String(raw['documentType'] ?? raw['type'] ?? ''),
      documentName: String(raw['documentName'] ?? raw['originalFilename'] ?? ''),
      documentPath: String(raw['documentPath'] ?? raw['downloadUrl'] ?? ''),
      originalFilename: raw['originalFilename'] ? String(raw['originalFilename']) : undefined,
      downloadUrl: raw['downloadUrl'] ? String(raw['downloadUrl']) : undefined,
      uploadedAt: raw['uploadedAt'] ? String(raw['uploadedAt']) : undefined,
      isImage: raw['isImage'] !== undefined ? Boolean(raw['isImage']) : undefined
    };
  }
}
