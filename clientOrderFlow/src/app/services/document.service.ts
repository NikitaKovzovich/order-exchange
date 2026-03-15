import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  Document,
  DocumentTypeInfo,
  EntityType,
  GeneratedDocument
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private readonly API_URL = '/api/documents';
  private readonly GENERATED_API_URL = '/api/generated-documents';

  constructor(private http: HttpClient) {}

  getDocumentTypes(): Observable<DocumentTypeInfo[]> {
    return this.http.get<ApiResponse<DocumentTypeInfo[]>>(`${this.API_URL}/types`).pipe(
      map(response => response.data || [])
    );
  }

  uploadDocument(file: File, documentTypeCode: string, entityType: EntityType, entityId: number): Observable<Document> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentTypeCode', documentTypeCode);
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

  getDocumentUrl(id: number): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.API_URL}/${id}/url`).pipe(
      map(response => response.data || '')
    );
  }

  getEntityDocuments(entityType: EntityType, entityId: number): Observable<Document[]> {
    return this.http.get<ApiResponse<Document[]>>(`${this.API_URL}/entity/${entityType}/${entityId}`).pipe(
      map(response => response.data || [])
    );
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getGeneratedDocumentsByOrder(orderId: number): Observable<GeneratedDocument[]> {
    return this.http.get<ApiResponse<GeneratedDocument[]>>(`${this.GENERATED_API_URL}/order/${orderId}`).pipe(
      map(response => response.data || [])
    );
  }

  getGeneratedDocument(id: number): Observable<GeneratedDocument> {
    return this.http.get<ApiResponse<GeneratedDocument>>(`${this.GENERATED_API_URL}/${id}`).pipe(
      map(response => response.data!)
    );
  }

  getGeneratedDocumentUrl(id: number): Observable<string> {
    return this.http.get<ApiResponse<string>>(`${this.GENERATED_API_URL}/${id}/url`).pipe(
      map(response => response.data || '')
    );
  }

  downloadGeneratedDocument(id: number): Observable<Blob> {
    return this.http.get(`${this.GENERATED_API_URL}/${id}/download`, {
      responseType: 'blob'
    });
  }

  generateTTN(orderId: number): Observable<GeneratedDocument> {
    return this.http.post<ApiResponse<GeneratedDocument>>(`${this.GENERATED_API_URL}/ttn`, { orderId }).pipe(
      map(response => response.data!)
    );
  }

  generateInvoice(orderId: number): Observable<GeneratedDocument> {
    return this.http.post<ApiResponse<GeneratedDocument>>(`${this.GENERATED_API_URL}/invoice`, { orderId }).pipe(
      map(response => response.data!)
    );
  }

  generateDiscrepancyAct(discrepancyReportId: number): Observable<GeneratedDocument> {
    return this.http.post<ApiResponse<GeneratedDocument>>(`${this.GENERATED_API_URL}/discrepancy-act`, { discrepancyReportId }).pipe(
      map(response => response.data!)
    );
  }
}

