import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequestDto, RegisterRequestDto, RegisterResponseDto } from '../type/auth.model';
import { env } from '../../env';

@Injectable({
  providedIn: 'root'
})
export class AuthApiService {
  private readonly apiUrl = `${env.API_URL}/v1/auth`;

  constructor(private http: HttpClient) {}

  login(dto: LoginRequestDto): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/login`, dto, { withCredentials: true });
  }

  register(dto: RegisterRequestDto): Observable<RegisterResponseDto> {
    return this.http.post<RegisterResponseDto>(`${this.apiUrl}/register`, dto, { withCredentials: true });
  }

  refresh(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/refresh`, {}, { withCredentials: true });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { withCredentials: true });
  }
}
