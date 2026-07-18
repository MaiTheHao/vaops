import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginRequestDto, RegisterRequestDto, RegisterResponseDto } from '../../type/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly baseUrl = '/api/v1/auth';

  constructor(private readonly http: HttpClient) {}

  login(credentials: LoginRequestDto): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/login`, credentials, {
      withCredentials: true
    });
  }

  register(user: RegisterRequestDto): Observable<RegisterResponseDto> {
    return this.http.post<RegisterResponseDto>(`${this.baseUrl}/register`, user, {
      withCredentials: true
    });
  }

  refresh(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/refresh`, {}, {
      withCredentials: true
    });
  }
}

