import { Injectable } from '@angular/core';
import {
  CompanyProfile,
  LoginRequest,
  LoginResponse,
  UserProfile
} from '../models/api.models';
import { ApiClientService } from './api-client.service';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly AUTH_PATH = 'auth';

  constructor(private apiClient: ApiClientService) {}

  login(credentials: LoginRequest) {
    return this.apiClient.post<LoginResponse>(`${this.AUTH_PATH}/login`, credentials);
  }

  getProfile() {
    return this.apiClient.get<UserProfile>(`${this.AUTH_PATH}/profile`);
  }

  getCompanyProfile(companyId: number) {
    return this.apiClient.get<CompanyProfile>(`${this.AUTH_PATH}/company/${companyId}`);
  }

  validateToken() {
    return this.apiClient.get<string>(`${this.AUTH_PATH}/validate`);
  }
}

