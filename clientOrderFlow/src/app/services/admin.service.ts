import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface User {
  id: number;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
  status: string;
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

export interface VerificationRequest {
  id: number;
  companyId: number;
  companyName: string;
  taxId: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  submittedAt: string;
  documents: VerificationDocument[];
}

export interface VerificationDocument {
  id: number;
  documentType: string;
  documentName: string;
  documentPath: string;
  uploadedAt: string;
}

export interface VerificationDetails extends VerificationRequest {
  company: {
    id: number;
    name?: string;
    legalName: string;
    legalForm: string;
    taxId: string;
    inn?: string;
    registrationDate: string;
    contactPhone: string;
    status: string;
    verified: boolean;
  };
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

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly API_URL = '/api/admin';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.API_URL}/users`).pipe(
      catchError(error => {
        console.error('Get users error:', error);
        return throwError(() => error);
      })
    );
  }

  getUserById(userId: number): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/users/${userId}`).pipe(
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

  getVerificationRequests(status?: 'PENDING' | 'APPROVED' | 'REJECTED'): Observable<VerificationRequest[]> {
    const url = status
      ? `${this.API_URL}/verification?status=${status}`
      : `${this.API_URL}/verification`;

    return this.http.get<VerificationRequest[]>(url).pipe(
      catchError(error => {
        console.error('Get verification requests error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationById(verificationId: number): Observable<VerificationDetails> {
    return this.http.get<VerificationDetails>(`${this.API_URL}/verification/${verificationId}`).pipe(
      catchError(error => {
        console.error('Get verification details error:', error);
        return throwError(() => error);
      })
    );
  }

  approveVerification(verificationId: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/verification/${verificationId}/approve`, {}).pipe(
      catchError(error => {
        console.error('Approve verification error:', error);
        return throwError(() => error);
      })
    );
  }

  rejectVerification(verificationId: number, reason: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.API_URL}/verification/${verificationId}/reject`,
      { reason }
    ).pipe(
      catchError(error => {
        console.error('Reject verification error:', error);
        return throwError(() => error);
      })
    );
  }

  getPendingVerificationRequests(): Observable<PendingVerificationRequest[]> {
    return this.http.get<PendingVerificationRequest[]>(`${this.API_URL}/verification/pending`).pipe(
      catchError(error => {
        console.error('Get pending verification requests error:', error);
        return throwError(() => error);
      })
    );
  }

  getVerificationDocuments(verificationId: number): Observable<VerificationDocument[]> {
    return this.http.get<VerificationDocument[]>(`${this.API_URL}/verification/${verificationId}/documents`).pipe(
      catchError(error => {
        console.error('Get verification documents error:', error);
        return throwError(() => error);
      })
    );
  }
}
