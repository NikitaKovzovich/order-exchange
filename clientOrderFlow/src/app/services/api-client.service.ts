import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpContext,
  HttpHeaders,
  HttpParams
} from '@angular/common/http';
import { Observable } from 'rxjs';

type PrimitiveParam = string | number | boolean;
type ApiParams = Record<string, PrimitiveParam | readonly PrimitiveParam[]>;

export interface ApiRequestOptions {
  headers?: HttpHeaders | Record<string, string | string[]>;
  context?: HttpContext;
  params?: HttpParams | ApiParams;
  observe?: 'body';
  reportProgress?: boolean;
  responseType?: 'json';
  withCredentials?: boolean;
  transferCache?: { includeHeaders?: string[] } | boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ApiClientService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  get<T>(path: string, options?: ApiRequestOptions): Observable<T> {
    return this.http.get<T>(this.buildUrl(path), options);
  }

  post<T>(path: string, body: unknown, options?: ApiRequestOptions): Observable<T> {
    return this.http.post<T>(this.buildUrl(path), body, options);
  }

  put<T>(path: string, body: unknown, options?: ApiRequestOptions): Observable<T> {
    return this.http.put<T>(this.buildUrl(path), body, options);
  }

  patch<T>(path: string, body: unknown, options?: ApiRequestOptions): Observable<T> {
    return this.http.patch<T>(this.buildUrl(path), body, options);
  }

  delete<T>(path: string, options?: ApiRequestOptions): Observable<T> {
    return this.http.delete<T>(this.buildUrl(path), options);
  }

  private buildUrl(path: string): string {
    if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith(this.API_URL)) {
      return path;
    }

    const normalizedPath = path.replace(/^\/+/, '');
    return `${this.API_URL}/${normalizedPath}`;
  }
}

