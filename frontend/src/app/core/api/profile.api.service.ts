import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AppConfigService } from '../services/app-config.service';
import {
  UserProfile,
  UpdateProfileReq,
  ChangePasswordReq,
} from '../../shared/models/profile.model';

@Injectable({
  providedIn: 'root',
})
export class ProfileApiService {
  private readonly http = inject(HttpClient);
  private readonly configService = inject(AppConfigService);

  private get apiUrl(): string {
    return `${this.configService.apiUrl()}/v1/profile`;
  }

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
