import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface RegistrationResponse {
  token: string;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
  userId: number;
  companyId: number;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
  private readonly API_URL = '/api/auth';

  constructor(private http: HttpClient) {}

  registerSupplier(formData: FormData): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.API_URL}/register`, formData).pipe(
      catchError(error => {
        console.error('Supplier registration error:', error);
        return throwError(() => error);
      })
    );
  }

  registerRetail(formData: FormData): Observable<RegistrationResponse> {
    return this.http.post<RegistrationResponse>(`${this.API_URL}/register`, formData).pipe(
      catchError(error => {
        console.error('Retail registration error:', error);
        return throwError(() => error);
      })
    );
  }
}
