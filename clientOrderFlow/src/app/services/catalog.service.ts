import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  ApiResponse,
  PageResponse,
  Category,
  CategoryTree,
  CreateCategoryRequest,
  Product,
  CreateProductRequest,
  UpdateProductRequest,
  ProductSearchParams,
  ProductImage,
  Inventory,
  UpdateInventoryRequest,
  Unit,
  VatRate
} from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private readonly API_URL = '/api';

  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<ApiResponse<Category[]>>(`${this.API_URL}/categories`).pipe(
      map(response => response.data || [])
    );
  }

  getCategoryTree(): Observable<CategoryTree[]> {
    return this.http.get<ApiResponse<CategoryTree[]>>(`${this.API_URL}/categories/tree`).pipe(
      map(response => response.data || [])
    );
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<ApiResponse<Category>>(`${this.API_URL}/categories/${id}`).pipe(
      map(response => response.data!)
    );
  }

  createCategory(request: CreateCategoryRequest): Observable<Category> {
    return this.http.post<ApiResponse<Category>>(`${this.API_URL}/categories`, request).pipe(
      map(response => response.data!)
    );
  }

  updateCategory(id: number, request: CreateCategoryRequest): Observable<Category> {
    return this.http.put<ApiResponse<Category>>(`${this.API_URL}/categories/${id}`, request).pipe(
      map(response => response.data!)
    );
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/categories/${id}`);
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<ApiResponse<Product>>(`${this.API_URL}/products/${id}`).pipe(
      map(response => response.data!)
    );
  }

  searchProducts(params: ProductSearchParams): Observable<PageResponse<Product>> {
    let httpParams = new HttpParams();

    if (params.categoryId) httpParams = httpParams.set('categoryId', params.categoryId.toString());
    if (params.supplierId) httpParams = httpParams.set('supplierId', params.supplierId.toString());
    if (params.minPrice) httpParams = httpParams.set('minPrice', params.minPrice.toString());
    if (params.maxPrice) httpParams = httpParams.set('maxPrice', params.maxPrice.toString());
    if (params.search) httpParams = httpParams.set('search', params.search);
    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<ApiResponse<PageResponse<Product>>>(`${this.API_URL}/products/search`, { params: httpParams }).pipe(
      map(response => response.data!)
    );
  }

  getSupplierProducts(params: ProductSearchParams): Observable<PageResponse<Product>> {
    let httpParams = new HttpParams();

    if (params.status) httpParams = httpParams.set('status', params.status);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<ApiResponse<PageResponse<Product>>>(`${this.API_URL}/products/supplier`, { params: httpParams }).pipe(
      map(response => response.data!)
    );
  }

  createProduct(request: CreateProductRequest): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`${this.API_URL}/products`, request).pipe(
      map(response => response.data!)
    );
  }

  updateProduct(id: number, request: UpdateProductRequest): Observable<Product> {
    return this.http.put<ApiResponse<Product>>(`${this.API_URL}/products/${id}`, request).pipe(
      map(response => response.data!)
    );
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/products/${id}`);
  }

  publishProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`${this.API_URL}/products/${id}/publish`, {}).pipe(
      map(response => response.data!)
    );
  }

  archiveProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`${this.API_URL}/products/${id}/archive`, {}).pipe(
      map(response => response.data!)
    );
  }

  draftProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<Product>>(`${this.API_URL}/products/${id}/draft`, {}).pipe(
      map(response => response.data!)
    );
  }

  getProductImages(productId: number): Observable<ProductImage[]> {
    return this.http.get<ApiResponse<ProductImage[]>>(`${this.API_URL}/products/${productId}/images`).pipe(
      map(response => response.data || [])
    );
  }

  uploadProductImage(productId: number, file: File, primary: boolean = false): Observable<ProductImage> {
    const formData = new FormData();
    formData.append('file', file);

    let params = new HttpParams();
    if (primary) params = params.set('primary', 'true');

    return this.http.post<ApiResponse<ProductImage>>(`${this.API_URL}/products/${productId}/images`, formData, { params }).pipe(
      map(response => response.data!)
    );
  }

  setPrimaryImage(productId: number, imageId: number): Observable<ProductImage> {
    return this.http.put<ApiResponse<ProductImage>>(`${this.API_URL}/products/${productId}/images/${imageId}/primary`, {}).pipe(
      map(response => response.data!)
    );
  }

  deleteProductImage(productId: number, imageId: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/products/${productId}/images/${imageId}`);
  }

  getInventory(productId: number): Observable<Inventory> {
    return this.http.get<ApiResponse<Inventory>>(`${this.API_URL}/inventory/${productId}`).pipe(
      map(response => response.data!)
    );
  }

  updateInventory(productId: number, request: UpdateInventoryRequest): Observable<Inventory> {
    return this.http.put<ApiResponse<Inventory>>(`${this.API_URL}/inventory/${productId}`, request).pipe(
      map(response => response.data!)
    );
  }

  addInventory(productId: number, request: UpdateInventoryRequest): Observable<Inventory> {
    return this.http.post<ApiResponse<Inventory>>(`${this.API_URL}/inventory/${productId}/add`, request).pipe(
      map(response => response.data!)
    );
  }

  getLowStockProducts(threshold: number = 10): Observable<Inventory[]> {
    return this.http.get<ApiResponse<Inventory[]>>(`${this.API_URL}/inventory/low-stock`, {
      params: { threshold: threshold.toString() }
    }).pipe(
      map(response => response.data || [])
    );
  }

  getOutOfStockProducts(): Observable<Inventory[]> {
    return this.http.get<ApiResponse<Inventory[]>>(`${this.API_URL}/inventory/out-of-stock`).pipe(
      map(response => response.data || [])
    );
  }

  getUnits(): Observable<Unit[]> {
    return this.http.get<ApiResponse<Unit[]>>(`${this.API_URL}/units`).pipe(
      map(response => response.data || [])
    );
  }

  getVatRates(): Observable<VatRate[]> {
    return this.http.get<ApiResponse<VatRate[]>>(`${this.API_URL}/vat-rates`).pipe(
      map(response => response.data || [])
    );
  }
}

