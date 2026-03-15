import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  CreatePartnershipRequest,
  Partnership,
  SupplierDirectoryItem,
  UpdatePartnershipContractRequest
} from '../models/api.models';
import { ApiClientService } from './api-client.service';

@Injectable({
  providedIn: 'root'
})
export class PartnershipService {
  private readonly API_URL = 'partnerships';

  constructor(private apiClient: ApiClientService) {}

  getCustomerSuppliers(search?: string): Observable<SupplierDirectoryItem[]> {
    const params = search ? { search } : undefined;
    return this.apiClient.get<ApiResponse<SupplierDirectoryItem[]>>(`${this.API_URL}/suppliers`, { params }).pipe(
      map(response => response.data || [])
    );
  }

  createPartnershipRequest(request: CreatePartnershipRequest): Observable<Partnership> {
    return this.apiClient.post<ApiResponse<Partnership>>(this.API_URL, request).pipe(
      map(response => response.data!)
    );
  }

  getCustomerPartnerships(): Observable<Partnership[]> {
    return this.apiClient.get<ApiResponse<Partnership[]>>(`${this.API_URL}/customer`).pipe(
      map(response => response.data || [])
    );
  }

  getSupplierPartnerships(): Observable<Partnership[]> {
    return this.apiClient.get<ApiResponse<Partnership[]>>(`${this.API_URL}/supplier`).pipe(
      map(response => response.data || [])
    );
  }

  getSupplierPendingPartnerships(): Observable<Partnership[]> {
    return this.apiClient.get<ApiResponse<Partnership[]>>(`${this.API_URL}/supplier/pending`).pipe(
      map(response => response.data || [])
    );
  }

  getSupplierActivePartnerships(): Observable<Partnership[]> {
    return this.apiClient.get<ApiResponse<Partnership[]>>(`${this.API_URL}/supplier/active`).pipe(
      map(response => response.data || [])
    );
  }

  acceptPartnership(id: number): Observable<Partnership> {
    return this.apiClient.post<ApiResponse<Partnership>>(`${this.API_URL}/${id}/accept`, {}).pipe(
      map(response => response.data!)
    );
  }

  rejectPartnership(id: number): Observable<Partnership> {
    return this.apiClient.post<ApiResponse<Partnership>>(`${this.API_URL}/${id}/reject`, {}).pipe(
      map(response => response.data!)
    );
  }

  updateContract(id: number, request: UpdatePartnershipContractRequest): Observable<Partnership> {
    return this.apiClient.put<ApiResponse<Partnership>>(`${this.API_URL}/${id}/contract`, request).pipe(
      map(response => response.data!)
    );
  }
}

