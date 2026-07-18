import { Injectable, signal } from '@angular/core';

import { UserProfile } from '../type/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthContextService {
  readonly isLoggedIn = signal<boolean>(false);
  readonly userProfile = signal<UserProfile | null>(null);

  setLogin(user: UserProfile) {
    this.isLoggedIn.set(true);
    this.userProfile.set(user);
  }

  setLogout() {
    this.isLoggedIn.set(false);
    this.userProfile.set(null);
  }
}
