import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  Document,
  DocumentType,
  EntityType
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly API_URL = '/api/documents';

  constructor(private http: HttpClient) {}

  uploadDocument(file: File, documentType: DocumentType, entityType: EntityType, entityId: number): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentType', documentType);
    formData.append('entityType', entityType);
    formData.append('entityId', entityId.toString());

    return this.http.post<ApiResponse<Document>>(this.API_URL, formData).pipe(
      map(response => response.data!)
    );
  }

  getDocument(id: number): Observable<Document> {
    return this.http.get<ApiResponse<Document>>(`${this.API_URL}/${id}`).pipe(
      map(response => response.data!)
    );
  }

  downloadDocument(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/download`, {
      responseType: 'blob'
    });
  }

  getEntityDocuments(entityType: EntityType, entityId: number): Observable<Document[]> {
    return this.http.get<ApiResponse<Document[]>>(`${this.API_URL}/entity/${entityType}/${entityId}`).pipe(
      map(response => response.data || [])
    );
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  generateTTN(orderId: number): Observable<Document> {
    return this.http.post<ApiResponse<Document>>(`${this.API_URL}/generate/ttn`, { orderId }).pipe(
      map(response => response.data!)
    );
  }

  generateDiscrepancyAct(discrepancyReportId: number): Observable<Document> {
    return this.http.post<ApiResponse<Document>>(`${this.API_URL}/generate/discrepancy-act`, { discrepancyReportId }).pipe(
      map(response => response.data!)
    );
  }
}

