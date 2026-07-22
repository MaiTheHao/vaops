import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { env } from '../../env';
import {
  UserProfile,
  UpdateProfileReq,
  ChangePasswordReq,
} from '../type/profile.model';

@Injectable({
  providedIn: 'root',
})
export class ProfileApiService {
  private readonly apiUrl = `${env.API_URL}/v1/profile`;

  constructor(private http: HttpClient) {}

  getMyProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(this.apiUrl, { withCredentials: true });
  }

  putUpdateProfile(dto: UpdateProfileReq): Observable<UserProfile> {
    return this.http.put<UserProfile>(this.apiUrl, dto, { withCredentials: true });
  }

  changePassword(dto: ChangePasswordReq): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/password`, dto, { withCredentials: true });
  }

  deleteAccount(): Observable<void> {
    return this.http.delete<void>(this.apiUrl, { withCredentials: true });
  }
}
