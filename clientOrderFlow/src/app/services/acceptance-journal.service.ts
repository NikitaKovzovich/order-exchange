import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { AcceptanceJournal, ApiResponse } from '../models/api.models';
import { ApiClientService } from './api-client.service';

export interface AcceptanceJournalParams {
  supplierId?: number;
  dateFrom?: string;
  dateTo?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AcceptanceJournalService {
  private readonly API_URL = 'acceptance-journal';

  constructor(private apiClient: ApiClientService) {}

  getJournal(params: AcceptanceJournalParams = {}): Observable<AcceptanceJournal> {
    const queryParams: Record<string, string> = {};

    if (params.supplierId !== undefined) {
      queryParams['supplierId'] = String(params.supplierId);
    }
    if (params.dateFrom) {
      queryParams['dateFrom'] = params.dateFrom;
    }
    if (params.dateTo) {
      queryParams['dateTo'] = params.dateTo;
    }

    return this.apiClient.get<ApiResponse<AcceptanceJournal>>(this.API_URL, { params: queryParams }).pipe(
      map(response => response.data || {
        details: [],
        summary: [],
        grandTotalQuantity: 0,
        grandTotalAmount: 0
      })
    );
  }
}

