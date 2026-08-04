import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { ErrorActionType } from '../../../shared/models/domain-error.model';

@Injectable({
  providedIn: 'root',
})
export class SilentErrorListener implements OnDestroy {
  private subscription: Subscription;

  constructor(private readonly errorBus: DomainErrorBusService) {
    this.subscription = this.errorBus.ofType(ErrorActionType.SILENT).subscribe((error) => {
      console.info(
        `[Silent Log] [${error.code}] Path trace logged silently. ReqId: ${error.requestId}`,
      );
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
