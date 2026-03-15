import { Injectable } from '@angular/core';
import { AuthSession, LoginResponse } from '../models/api.models';

@Injectable({
  providedIn: 'root'
})
export class AuthStorageService {
  private readonly tokenKey = 'jwt_token';
  private readonly sessionKey = 'current_user';
  private readonly rememberMeKey = 'remember_me';

  getToken(): string | null {
    return this.readValue(this.tokenKey);
  }

  getSession(): AuthSession | null {
    const rawValue = this.readValue(this.sessionKey);
    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue) as AuthSession;
    } catch {
      this.removeFromAllStorages(this.sessionKey);
      return null;
    }
  }

  saveAuth(response: LoginResponse, rememberMe: boolean): void {
    this.setRememberMe(rememberMe);
    this.clearAuthData();

    const storage = this.resolveStorage(rememberMe);
    const session = this.createSession(response);

    storage.setItem(this.tokenKey, response.token);
    storage.setItem(this.sessionKey, JSON.stringify(session));
  }

  saveSession(session: AuthSession, rememberMe: boolean = this.getRememberMe()): void {
    this.removeFromAllStorages(this.sessionKey);
    this.resolveStorage(rememberMe).setItem(this.sessionKey, JSON.stringify(session));
  }

  saveToken(token: string, rememberMe: boolean = this.getRememberMe()): void {
    this.removeFromAllStorages(this.tokenKey);
    this.resolveStorage(rememberMe).setItem(this.tokenKey, token);
  }

  clear(): void {
    this.clearAuthData();
    localStorage.removeItem(this.rememberMeKey);
  }

  setRememberMe(rememberMe: boolean): void {
    localStorage.setItem(this.rememberMeKey, String(rememberMe));
  }

  getRememberMe(): boolean {
    return localStorage.getItem(this.rememberMeKey) === 'true';
  }

  private clearAuthData(): void {
    this.removeFromAllStorages(this.tokenKey);
    this.removeFromAllStorages(this.sessionKey);
  }

  private createSession(response: LoginResponse): AuthSession {
    return {
      email: response.email,
      role: response.role,
      userId: response.userId,
      companyId: response.companyId
    };
  }

  private readValue(key: string): string | null {
    return localStorage.getItem(key) || sessionStorage.getItem(key);
  }

  private removeFromAllStorages(key: string): void {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  }

  private resolveStorage(rememberMe: boolean): Storage {
    return rememberMe ? localStorage : sessionStorage;
  }
}

