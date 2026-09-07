import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { finalize, shareReplay } from 'rxjs/operators';
import { AuthApiService } from '../api/auth.api.service';

@Injectable({
  providedIn: 'root',
})
export class AuthSessionService {
  private readonly authApi = inject(AuthApiService);

  private refreshInFlight$: Observable<void> | null = null;
  private redirectEmitted = false;

  refreshToken(): Observable<void> {
    if (!this.refreshInFlight$) {
      this.redirectEmitted = false;
      this.refreshInFlight$ = this.authApi.refresh().pipe(
        shareReplay({ bufferSize: 1, refCount: false }),
        finalize(() => {
          this.refreshInFlight$ = null;
        }),
      );
    }

    return this.refreshInFlight$;
  }

  isRedirectEmitted(): boolean {
    return this.redirectEmitted;
  }

  markRedirectEmitted(): void {
    this.redirectEmitted = true;
  }
}
