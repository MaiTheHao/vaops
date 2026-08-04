import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequestDto, RegisterRequestDto, RegisterResponseDto } from '../../shared/models/auth.model';
import { env } from '../../../env';
import { SKIP_ERROR_EMISSION } from './http-context.tokens';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly apiUrl = `${env.API_URL}/v1/auth`;

  constructor(private http: HttpClient) {}

  login(dto: LoginRequestDto): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/login`, dto, {
      withCredentials: true,
      context: new HttpContext().set(SKIP_ERROR_EMISSION, true),
    });
  }

  register(dto: RegisterRequestDto): Observable<RegisterResponseDto> {
    return this.http.post<RegisterResponseDto>(`${this.apiUrl}/register`, dto, {
      withCredentials: true,
      context: new HttpContext().set(SKIP_ERROR_EMISSION, true),
    });
  }

  refresh(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, {
      withCredentials: true,
      context: new HttpContext().set(SKIP_ERROR_EMISSION, true),
    });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, {
      withCredentials: true,
      context: new HttpContext().set(SKIP_ERROR_EMISSION, true),
    });
  }
}
