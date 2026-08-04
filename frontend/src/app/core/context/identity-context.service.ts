import { inject, Injectable, signal } from '@angular/core';
import { ProfileApiService } from '../api/profile.api.service';
import { EventBusService } from '../services/event-bus.service';
import { AppEventKey } from '../constants/app-event.const';
import { UserProfile } from '../../shared/models/profile.model';

@Injectable({
  providedIn: 'root',
})
export class IdentityContextService {
  readonly userProfile = signal<UserProfile | null>(null);

  private readonly profileApi = inject(ProfileApiService);
  private readonly EventBusService = inject(EventBusService);

  constructor() {
    this.initEventListeners();
  }

  private initEventListeners(): void {
    this.EventBusService
      .listen<void>([AppEventKey.APP_INIT, AppEventKey.LOGIN_SUCCESS, AppEventKey.PROFILE_CHANGED])
      .subscribe(() => {
        this.fetchProfile();
      });

    this.EventBusService.listen<void>(AppEventKey.LOGOUT).subscribe(() => {
      this.clearProfile();
      this.EventBusService.publish(AppEventKey.PROFILE_CLEARED);
    });
  }

  fetchProfile(): void {
    this.profileApi.getMyProfile().subscribe({
      next: (profile) => {
        this.setProfile(profile);
        this.EventBusService.publish(AppEventKey.PROFILE_SYNCED, profile);
      },
      error: () => {
        this.clearProfile();
      },
    });
  }

  setProfile(user: UserProfile): void {
    this.userProfile.set(user);
  }

  clearProfile(): void {
    this.userProfile.set(null);
  }
}
