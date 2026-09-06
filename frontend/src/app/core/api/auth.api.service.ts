import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  LoginRequestDto,
  RegisterRequestDto,
  RegisterResponseDto,
} from '../../shared/models/auth.model';
import { AppConfigService } from '../services/app-config.service';
import { BYPASS_GLOBAL_ERROR_CONTEXT } from './request-error.policy';

@Injectable({
  providedIn: 'root',
})
export class AuthApiService {
  private readonly http = inject(HttpClient);
  private readonly configService = inject(AppConfigService);

  private get apiUrl(): string {
    return `${this.configService.apiUrl()}/v1/auth`;
  }

  login(dto: LoginRequestDto): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/login`, dto, {
      withCredentials: true,
      context: BYPASS_GLOBAL_ERROR_CONTEXT,
    });
  }

  register(dto: RegisterRequestDto): Observable<RegisterResponseDto> {
    return this.http.post<RegisterResponseDto>(`${this.apiUrl}/register`, dto, {
      withCredentials: true,
      context: BYPASS_GLOBAL_ERROR_CONTEXT,
    });
  }

  refresh(): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/refresh`,
      {},
      {
        withCredentials: true,
        context: BYPASS_GLOBAL_ERROR_CONTEXT,
      },
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/logout`,
      {},
      {
        withCredentials: true,
        context: BYPASS_GLOBAL_ERROR_CONTEXT,
      },
    );
  }
}
