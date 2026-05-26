import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CompanyAddress } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private readonly API_URL = '/api/addresses';

  constructor(private http: HttpClient) {}

  getCompanyAddresses(companyId: number): Observable<CompanyAddress[]> {
    return this.http.get<CompanyAddress[]>(`${this.API_URL}/company/${companyId}`).pipe(
      catchError(() => of([]))
    );
  }

  addAddress(request: { addressType: string; fullAddress: string; isDefault?: boolean }): Observable<CompanyAddress> {
    return this.http.post<CompanyAddress>('/api/addresses', request);
  }
}

