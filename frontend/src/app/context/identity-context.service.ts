import { Injectable, signal } from '@angular/core';
import { UserProfile } from '../type/auth.model';

@Injectable({
  providedIn: 'root'
})
export class IdentityContextService {
  readonly userProfile = signal<UserProfile | null>(null);

  setProfile(user: UserProfile) {
    this.userProfile.set(user);
  }

  clearProfile() {
    this.userProfile.set(null);
  }
}
