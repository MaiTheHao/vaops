import { Injectable, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { ErrorActionType } from '../../../shared/models/domain-error.model';
import { DialogFactoryService } from '../../../shared/components/dialogs/dialog-factory.service';

@Injectable({
  providedIn: 'root',
})
export class DialogErrorListener implements OnDestroy {
  private subscription: Subscription;

  constructor(
    private readonly errorBus: DomainErrorBusService,
    private readonly dialog: DialogFactoryService,
  ) {
    this.subscription = this.errorBus.ofType(ErrorActionType.DIALOG).subscribe((error) => {
      this.dialog.openDomainError(error).subscribe();
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
