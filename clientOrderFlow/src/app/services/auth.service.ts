import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import {
  AuthSession,
  CompanyProfile,
  LoginRequest,
  LoginResponse,
  UserProfile,
  UserRole
} from '../models/api.models';
import { AuthApiService } from './auth-api.service';
import { AuthStorageService } from './auth-storage.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private tokenSubject: BehaviorSubject<string | null>;
  private currentUserSubject: BehaviorSubject<AuthSession | null>;

  public token$: Observable<string | null>;
  public currentUser$: Observable<AuthSession | null>;

  constructor(
    private authApi: AuthApiService,
    private authStorage: AuthStorageService
  ) {
    this.tokenSubject = new BehaviorSubject<string | null>(this.authStorage.getToken());
    this.currentUserSubject = new BehaviorSubject<AuthSession | null>(this.authStorage.getSession());
    this.token$ = this.tokenSubject.asObservable();
    this.currentUser$ = this.currentUserSubject.asObservable();

    const token = this.authStorage.getToken();
    const user = this.authStorage.getSession();

    if (token && !user) {
      this.refreshProfile().subscribe({
        next: () => {
          this.tokenSubject.next(this.authStorage.getToken());
        },
        error: () => {
          this.logout();
        }
      });
    }
  }

  login(credentials: LoginRequest, rememberMe: boolean = false): Observable<LoginResponse> {
    return this.authApi.login(credentials).pipe(
      tap(response => {
        this.authStorage.saveAuth(response, rememberMe);
        this.tokenSubject.next(response.token);
        this.currentUserSubject.next(this.authStorage.getSession());
      }),
      catchError(error => {
        console.error('Login error:', error);
        return throwError(() => error);
      })
    );
  }

  getProfile(): Observable<UserProfile> {
    return this.authApi.getProfile().pipe(
      tap(profile => this.syncSessionFromProfile(profile)),
      catchError(error => {
        console.error('Get profile error:', error);
        return throwError(() => error);
      })
    );
  }

  getCompanyProfile(companyId: number): Observable<CompanyProfile> {
    return this.authApi.getCompanyProfile(companyId).pipe(
      catchError(error => {
        console.error('Get company profile error:', error);
        return throwError(() => error);
      })
    );
  }

  validateToken(): Observable<any> {
    const token = this.authStorage.getToken();
    if (!token) {
      return throwError(() => new Error('No token'));
    }

    return this.authApi.validateToken().pipe(
      catchError(error => {
        console.error('Token validation error:', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  refreshProfile(): Observable<UserProfile> {
    return this.authApi.getProfile().pipe(
      tap(profile => this.syncSessionFromProfile(profile))
    );
  }

  logout(): void {
    this.authStorage.clear();
    this.tokenSubject.next(null);
    this.currentUserSubject.next(null);
  }

  isAuthenticated(): boolean {
    return this.hasValidSession();
  }

  hasValidSession(): boolean {
    return !!this.authStorage.getToken();
  }

  getToken(): string | null {
    return this.authStorage.getToken();
  }

  getCurrentUser(): AuthSession | null {
    return this.currentUserSubject.value;
  }

  getSession(): AuthSession | null {
    return this.currentUserSubject.value;
  }

  setRememberMe(remember: boolean): void {
    this.authStorage.setRememberMe(remember);
  }

  getUserRole(): UserRole | null {
    const user = this.getCurrentUser();
    return user?.role || null;
  }

  getUserId(): number | null {
    return this.getCurrentUser()?.userId ?? null;
  }

  getCompanyId(): number | null {
    return this.getCurrentUser()?.companyId ?? null;
  }

  hasRole(role: UserRole): boolean {
    return this.getUserRole() === role;
  }

  hasAnyRole(roles: readonly UserRole[]): boolean {
    const userRole = this.getUserRole();
    return !!userRole && roles.includes(userRole);
  }

  getDefaultRoute(): string {
    return this.getDefaultRouteForRole(this.getUserRole());
  }

  getDefaultRouteForRole(role: UserRole | null | undefined): string {
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'SUPPLIER':
        return '/supplier/dashboard';
      case 'RETAIL_CHAIN':
        return '/retail/dashboard';
      default:
        return '/';
    }
  }

  private syncSessionFromProfile(profile: UserProfile): void {
    const token = this.authStorage.getToken();
    if (!token) {
      return;
    }

    const session: AuthSession = {
      email: profile.email,
      role: profile.role,
      userId: profile.id,
      companyId: profile.companyId
    };

    this.authStorage.saveSession(session);
    this.tokenSubject.next(token);
    this.currentUserSubject.next(session);
  }
}
