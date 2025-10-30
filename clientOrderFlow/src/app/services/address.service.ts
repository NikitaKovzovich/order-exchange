import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Address {
  id: number;
  companyId: number;
  addressType: 'legal' | 'postal' | 'delivery';
  fullAddress: string;
  isDefault: boolean;
}

export interface CreateAddressRequest {
  addressType: 'legal' | 'postal' | 'delivery';
  fullAddress: string;
  isDefault?: boolean;
}

export interface UpdateAddressRequest extends CreateAddressRequest {
  id: number;
}

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private readonly API_URL = '/api/addresses';

  constructor(private http: HttpClient) {}

  /**
   * Get company addresses
   */
  getCompanyAddresses(companyId: number): Observable<Address[]> {
    return this.http.get<Address[]>(`${this.API_URL}/company/${companyId}`);
  }

  /**
   * Add new address
   */
  addAddress(data: CreateAddressRequest): Observable<Address> {
    return this.http.post<Address>(this.API_URL, data);
  }

  /**
   * Update address
   */
  updateAddress(addressId: number, data: CreateAddressRequest): Observable<Address> {
    return this.http.put<Address>(`${this.API_URL}/${addressId}`, data);
  }

  /**
   * Delete address
   */
  deleteAddress(addressId: number): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/${addressId}`);
  }

  /**
   * Set default address
   */
  setDefaultAddress(addressId: number): Observable<Address> {
    return this.http.post<Address>(`${this.API_URL}/${addressId}/default`, {});
  }
}
