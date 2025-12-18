import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { ApiResponse, CompanyAddress } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private readonly API_URL = '/api/auth';

  constructor(private http: HttpClient) {}

  getCompanyAddresses(companyId: number): Observable<CompanyAddress[]> {
    return this.http.get<ApiResponse<CompanyAddress[]>>(`${this.API_URL}/company/addresses`).pipe(
      map(response => response.data || []),
      catchError(() => of([
        { id: 1, addressType: 'WAREHOUSE', fullAddress: 'г. Минск, ул. Торговая, д. 5 (Основной склад)', isDefault: true },
        { id: 2, addressType: 'STORE', fullAddress: 'г. Минск, пр. Победителей, д. 100 (Магазин №1)', isDefault: false }
      ]))
    );
  }
}

