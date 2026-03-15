import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LoginResponse } from '../models/api.models';
import { ApiClientService } from './api-client.service';

export interface RegistrationResponse extends LoginResponse {
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {
  private readonly API_URL = 'auth';

  constructor(private apiClient: ApiClientService) {}

  registerSupplier(formData: FormData): Observable<RegistrationResponse> {
    return this.apiClient.post<RegistrationResponse>(`${this.API_URL}/register`, formData).pipe(
      catchError(error => {
        console.error('Supplier registration error:', error);
        return throwError(() => error);
      })
    );
  }

  registerRetail(formData: FormData): Observable<RegistrationResponse> {
    return this.apiClient.post<RegistrationResponse>(`${this.API_URL}/register`, formData).pipe(
      catchError(error => {
        console.error('Retail registration error:', error);
        return throwError(() => error);
      })
    );
  }
}
