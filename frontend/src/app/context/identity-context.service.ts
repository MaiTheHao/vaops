import { inject, Injectable, signal } from '@angular/core';
import { ProfileApiService } from '../api/profile.api.service';
import { EventManager } from '../shared/service/event-manager.service';
import { AppEventKey } from '../shared/const/app-event.const';
import { UserProfile } from '../type/profile.model';

@Injectable({
  providedIn: 'root',
})
export class IdentityContextService {
  readonly userProfile = signal<UserProfile | null>(null);

  private readonly profileApi = inject(ProfileApiService);
  private readonly eventManager = inject(EventManager);

  constructor() {
    this.initEventListeners();
  }

  private initEventListeners(): void {
    this.eventManager
      .listen<void>([
        AppEventKey.APP_INIT,
        AppEventKey.LOGIN_SUCCESS,
        AppEventKey.PROFILE_CHANGED,
      ])
      .subscribe(() => {
        this.fetchProfile();
      });

    this.eventManager.listen<void>(AppEventKey.LOGOUT).subscribe(() => {
      this.clearProfile();
      this.eventManager.publish(AppEventKey.PROFILE_CLEARED);
    });
  }

  fetchProfile(): void {
    this.profileApi.getMyProfile().subscribe({
      next: (profile) => {
        this.setProfile(profile);
        this.eventManager.publish(AppEventKey.PROFILE_SYNCED, profile);
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
