import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, shareReplay, tap } from 'rxjs/operators';
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
  private categories$?: Observable<Category[]>;
  private categoryTree$?: Observable<CategoryTree[]>;
  private units$?: Observable<Unit[]>;
  private vatRates$?: Observable<VatRate[]>;

  constructor(private http: HttpClient) {}

  private invalidateCategoryCache(): void {
    this.categories$ = undefined;
    this.categoryTree$ = undefined;
  }

  private invalidateReferenceDataCache(): void {
    this.units$ = undefined;
    this.vatRates$ = undefined;
  }

  getCategories(): Observable<Category[]> {
    if (!this.categories$) {
      this.categories$ = this.http.get<ApiResponse<Category[]>>(`${this.API_URL}/categories`).pipe(
        map(response => response.data || []),
        shareReplay(1)
      );
    }

    return this.categories$;
  }

  getCategoryTree(): Observable<CategoryTree[]> {
    if (!this.categoryTree$) {
      this.categoryTree$ = this.http.get<ApiResponse<CategoryTree[]>>(`${this.API_URL}/categories/tree`).pipe(
        map(response => response.data || []),
        shareReplay(1)
      );
    }

    return this.categoryTree$;
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<ApiResponse<Category>>(`${this.API_URL}/categories/${id}`).pipe(
      map(response => response.data!)
    );
  }

  createCategory(request: CreateCategoryRequest): Observable<Category> {
    return this.http.post<ApiResponse<Category>>(`${this.API_URL}/categories`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateCategoryCache())
    );
  }

  updateCategory(id: number, request: CreateCategoryRequest): Observable<Category> {
    return this.http.put<ApiResponse<Category>>(`${this.API_URL}/categories/${id}`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateCategoryCache())
    );
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/categories/${id}`).pipe(
      tap(() => this.invalidateCategoryCache())
    );
  }

  getProductById(id: number): Observable<Product> {
    return this.http.get<ApiResponse<unknown>>(`${this.API_URL}/products/${id}`).pipe(
      map(response => this.mapProduct(response.data))
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

    return this.http.get<ApiResponse<PageResponse<unknown>>>(`${this.API_URL}/products/search`, { params: httpParams }).pipe(
      map(response => this.mapProductPage(response.data, params.page, params.size))
    );
  }

  getSupplierProducts(params: ProductSearchParams): Observable<PageResponse<Product>> {
    let httpParams = new HttpParams();
    const normalizedSearch = params.search?.trim();

    if (params.status) httpParams = httpParams.set('status', params.status);
    if (normalizedSearch) httpParams = httpParams.set('search', normalizedSearch);
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<ApiResponse<PageResponse<unknown>>>(`${this.API_URL}/products/supplier`, { params: httpParams }).pipe(
      map(response => this.mapProductPage(response.data, params.page, params.size))
    );
  }

  createProduct(request: CreateProductRequest): Observable<Product> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/products`, request).pipe(
      map(response => this.mapProduct(response.data))
    );
  }

  updateProduct(id: number, request: UpdateProductRequest): Observable<Product> {
    return this.http.put<ApiResponse<unknown>>(`${this.API_URL}/products/${id}`, request).pipe(
      map(response => this.mapProduct(response.data))
    );
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/products/${id}`);
  }

  publishProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/products/${id}/publish`, {}).pipe(
      map(response => this.mapProduct(response.data))
    );
  }

  archiveProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/products/${id}/archive`, {}).pipe(
      map(response => this.mapProduct(response.data))
    );
  }

  draftProduct(id: number): Observable<Product> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/products/${id}/draft`, {}).pipe(
      map(response => this.mapProduct(response.data))
    );
  }

  getProductImages(productId: number): Observable<ProductImage[]> {
    return this.http.get<ApiResponse<unknown[]>>(`${this.API_URL}/products/${productId}/images`).pipe(
      map(response => (response.data || []).map(image => this.mapProductImage(image)))
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
    return this.http.get<ApiResponse<unknown>>(`${this.API_URL}/inventory/${productId}`).pipe(
      map(response => this.mapInventory(response.data))
    );
  }

  updateInventory(productId: number, request: UpdateInventoryRequest): Observable<Inventory> {
    return this.http.put<ApiResponse<unknown>>(`${this.API_URL}/inventory/${productId}`, request).pipe(
      map(response => this.mapInventory(response.data))
    );
  }

  addInventory(productId: number, request: UpdateInventoryRequest): Observable<Inventory> {
    return this.http.post<ApiResponse<unknown>>(`${this.API_URL}/inventory/${productId}/add`, request).pipe(
      map(response => this.mapInventory(response.data))
    );
  }

  getLowStockProducts(threshold: number = 10): Observable<Inventory[]> {
    return this.http.get<ApiResponse<Inventory[]>>(`${this.API_URL}/inventory/low-stock`, {
      params: { threshold: threshold.toString() }
    }).pipe(
      map(response => (response.data || []).map(item => this.mapInventory(item)))
    );
  }

  getOutOfStockProducts(): Observable<Inventory[]> {
    return this.http.get<ApiResponse<Inventory[]>>(`${this.API_URL}/inventory/out-of-stock`).pipe(
      map(response => (response.data || []).map(item => this.mapInventory(item)))
    );
  }

  getUnits(): Observable<Unit[]> {
    if (!this.units$) {
      this.units$ = this.http.get<ApiResponse<Unit[]>>(`${this.API_URL}/units`).pipe(
        map(response => response.data || []),
        shareReplay(1)
      );
    }

    return this.units$;
  }

  createUnit(request: { name: string }): Observable<Unit> {
    return this.http.post<ApiResponse<Unit>>(`${this.API_URL}/units`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  updateUnit(id: number, request: { name: string }): Observable<Unit> {
    return this.http.put<ApiResponse<Unit>>(`${this.API_URL}/units/${id}`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  deleteUnit(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/units/${id}`).pipe(
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  getVatRates(): Observable<VatRate[]> {
    if (!this.vatRates$) {
      this.vatRates$ = this.http.get<ApiResponse<VatRate[]>>(`${this.API_URL}/vat-rates`).pipe(
        map(response => response.data || []),
        shareReplay(1)
      );
    }

    return this.vatRates$;
  }

  createVatRate(request: { description: string; ratePercentage: number }): Observable<VatRate> {
    return this.http.post<ApiResponse<VatRate>>(`${this.API_URL}/vat-rates`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  updateVatRate(id: number, request: { description: string; ratePercentage: number }): Observable<VatRate> {
    return this.http.put<ApiResponse<VatRate>>(`${this.API_URL}/vat-rates/${id}`, request).pipe(
      map(response => response.data!),
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  deleteVatRate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/vat-rates/${id}`).pipe(
      tap(() => this.invalidateReferenceDataCache())
    );
  }

  private mapProductPage(data: PageResponse<unknown> | undefined, page: number | undefined, size: number | undefined): PageResponse<Product> {
    return {
      content: (data?.content || []).map(item => this.mapProduct(item)),
      totalElements: Number(data?.totalElements ?? 0),
      totalPages: Number(data?.totalPages ?? 0),
      size: Number(data?.size ?? size ?? 20),
      number: Number(data?.number ?? page ?? 0),
      first: data?.first,
      last: data?.last
    };
  }

  private mapProduct(item: unknown): Product {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      supplierId: Number(raw['supplierId'] ?? 0),
      supplierName: raw['supplierName'] ? String(raw['supplierName']) : undefined,
      sku: String(raw['sku'] ?? ''),
      name: String(raw['name'] ?? ''),
      description: String(raw['description'] ?? ''),
      category: this.mapCategory(raw['category']),
      pricePerUnit: Number(raw['pricePerUnit'] ?? 0),
      priceWithVat: Number(raw['priceWithVat'] ?? raw['pricePerUnit'] ?? 0),
      unitOfMeasure: String(raw['unitOfMeasure'] ?? raw['unitName'] ?? ''),
      vatRateName: String(raw['vatRateName'] ?? raw['vatRateDescription'] ?? ''),
      vatRateValue: Number(raw['vatRateValue'] ?? raw['vatPercentage'] ?? 0),
      weight: Number(raw['weight'] ?? 0),
      countryOfOrigin: String(raw['countryOfOrigin'] ?? ''),
      barcode: String(raw['barcode'] ?? ''),
      primaryImageUrl: raw['primaryImageUrl'] ? String(raw['primaryImageUrl']) : null,
      status: String(raw['status'] ?? 'DRAFT') as Product['status'],
      quantityAvailable: Number(raw['quantityAvailable'] ?? raw['availableQuantity'] ?? 0),
      inStock: Boolean(raw['inStock']),
      packageDimensions: raw['packageDimensions'] ? String(raw['packageDimensions']) : null,
      productionDate: raw['productionDate'] ? String(raw['productionDate']) : null,
      expiryDate: raw['expiryDate'] ? String(raw['expiryDate']) : null,
      createdAt: String(raw['createdAt'] ?? ''),
      updatedAt: raw['updatedAt'] ? String(raw['updatedAt']) : undefined
    };
  }

  private mapCategory(item: unknown): Category {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      name: String(raw['name'] ?? ''),
      parentId: raw['parentId'] !== undefined && raw['parentId'] !== null ? Number(raw['parentId']) : null,
      parentName: raw['parentName'] !== undefined && raw['parentName'] !== null ? String(raw['parentName']) : null,
      productCount: Number(raw['productCount'] ?? 0)
    };
  }

  private mapProductImage(item: unknown): ProductImage {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      id: Number(raw['id'] ?? 0),
      productId: Number(raw['productId'] ?? 0),
      fileName: String(raw['fileName'] ?? ''),
      contentType: String(raw['contentType'] ?? raw['mimeType'] ?? ''),
      fileSize: Number(raw['fileSize'] ?? raw['sizeKb'] ?? 0),
      primary: Boolean(raw['primary'] ?? raw['isPrimary']),
      sortOrder: Number(raw['sortOrder'] ?? 0),
      imageUrl: String(raw['imageUrl'] ?? raw['url'] ?? '')
    };
  }

  private mapInventory(item: unknown): Inventory {
    const raw = (item || {}) as Record<string, unknown>;

    return {
      productId: Number(raw['productId'] ?? 0),
      productName: String(raw['productName'] ?? ''),
      sku: String(raw['sku'] ?? raw['productSku'] ?? ''),
      quantityAvailable: Number(raw['quantityAvailable'] ?? 0),
      reservedQuantity: Number(raw['reservedQuantity'] ?? 0),
      actualAvailable: Number(raw['actualAvailable'] ?? 0),
      lowStock: Boolean(raw['lowStock']),
      outOfStock: Boolean(raw['outOfStock'])
    };
  }
}

