import { Injectable, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { ErrorActionType } from '../../../shared/models/domain-error.model';

@Injectable({
  providedIn: 'root',
})
export class RedirectErrorListener implements OnDestroy {
  private subscription: Subscription;

  constructor(
    private readonly errorBus: DomainErrorBusService,
    private readonly router: Router,
  ) {
    this.subscription = this.errorBus.ofType(ErrorActionType.REDIRECT).subscribe((error) => {
      const targetUrl = error.redirectUrl || '/auth';
      const currentUrl = this.router.url;
      this.router.navigate(
        [targetUrl],
        currentUrl && currentUrl !== targetUrl
          ? { queryParams: { returnUrl: currentUrl } }
          : undefined,
      );
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
