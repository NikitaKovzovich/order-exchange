import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
  userId: number;
  companyId: number;
}

export interface UserProfile {
  id: number;
  email: string;
  role: 'ADMIN' | 'SUPPLIER' | 'RETAIL_CHAIN';
  companyId: number;
}

export interface CompanyProfile {
  id: number;
  legalName: string;
  legalForm: string;
  taxId: string;
  registrationDate: string;
  status: 'PENDING' | 'ACTIVE' | 'REJECTED' | 'BLOCKED';
  contactPhone: string;
  addresses: Array<{
    id: number;
    addressType: string;
    fullAddress: string;
    isDefault: boolean;
  }>;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  private tokenSubject = new BehaviorSubject<string | null>(this.getStoredToken());
  private currentUserSubject = new BehaviorSubject<Partial<LoginResponse> | null>(this.getStoredUser());

  public token$ = this.tokenSubject.asObservable();
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const token = this.getStoredToken();
    const user = this.getStoredUser();

    if (token && !user) {
      this.getProfile().subscribe({
        next: (profile) => {
          const userData = {
            email: profile.email,
            role: profile.role,
            userId: profile.id,
            companyId: profile.companyId
          };
          this.setUser(userData as LoginResponse);
        },
        error: () => {
          this.logout();
        }
      });
    }
  }

  login(credentials: LoginRequest, rememberMe: boolean = false): Observable<LoginResponse> {
    this.setRememberMe(rememberMe);

    return this.http.post<LoginResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => {
        this.setToken(response.token);
        this.setUser(response);
      }),
      catchError(error => {
        console.error('Login error:', error);
        return throwError(() => error);
      })
    );
  }

  getProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${this.API_URL}/profile`).pipe(
      catchError(error => {
        console.error('Get profile error:', error);
        return throwError(() => error);
      })
    );
  }

  getCompanyProfile(companyId: number): Observable<CompanyProfile> {
    return this.http.get<CompanyProfile>(`${this.API_URL}/company/${companyId}`).pipe(
      catchError(error => {
        console.error('Get company profile error:', error);
        return throwError(() => error);
      })
    );
  }

  validateToken(): Observable<any> {
    const token = this.getStoredToken();
    if (!token) {
      return throwError(() => new Error('No token'));
    }

    return this.http.get(`${this.API_URL}/validate`).pipe(
      catchError(error => {
        console.error('Token validation error:', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    sessionStorage.removeItem('jwt_token');
    localStorage.removeItem('current_user');
    sessionStorage.removeItem('current_user');
    localStorage.removeItem('remember_me');
    this.tokenSubject.next(null);
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return !!this.getStoredToken();
  }

  getToken(): string | null {
    return this.getStoredToken();
  }

  getCurrentUser(): Partial<LoginResponse> | null {
    return this.currentUserSubject.value;
  }

  setRememberMe(remember: boolean): void {
    localStorage.setItem('remember_me', remember.toString());
  }

  getUserRole(): string | null {
    const user = this.getCurrentUser();
    return user?.role || null;
  }

  private setToken(token: string): void {
    const rememberMe = localStorage.getItem('remember_me') === 'true';

    if (rememberMe) {
      localStorage.setItem('jwt_token', token);
    } else {
      sessionStorage.setItem('jwt_token', token);
    }

    this.tokenSubject.next(token);
  }

  private setUser(user: LoginResponse): void {
    const rememberMe = localStorage.getItem('remember_me') === 'true';
    const userData = {
      email: user.email,
      role: user.role,
      userId: user.userId,
      companyId: user.companyId
    };

    if (rememberMe) {
      localStorage.setItem('current_user', JSON.stringify(userData));
    } else {
      sessionStorage.setItem('current_user', JSON.stringify(userData));
    }

    this.currentUserSubject.next(userData);
  }

  private getStoredToken(): string | null {
    return localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token');
  }

  private getStoredUser(): Partial<LoginResponse> | null {
    const userStr = localStorage.getItem('current_user') || sessionStorage.getItem('current_user');
    return userStr ? JSON.parse(userStr) : null;
  }
}
