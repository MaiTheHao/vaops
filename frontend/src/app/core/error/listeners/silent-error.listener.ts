import { Injectable, OnDestroy, inject } from '@angular/core';
import { Subscription } from 'rxjs';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { ErrorActionType } from '../../../shared/models/domain-error.model';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { LanguageService } from '../../services/language.service';
import { TranslateKey } from '../../constants/translate-key.const';

@Injectable({
  providedIn: 'root',
})
export class SilentErrorListener implements OnDestroy {
  private readonly toastService = inject(ToastService);
  private readonly languageService = inject(LanguageService);
  private subscription: Subscription;

  constructor(private readonly errorBus: DomainErrorBusService) {
    this.subscription = this.errorBus.ofType(ErrorActionType.SILENT).subscribe((error) => {
      console.info(
        `[Silent Log] [${error.code}] Path trace logged silently. ReqId: ${error.requestId}`,
      );

      const domainErrorKeys = TranslateKey.error.domain;
      const errorCodeKey = error.code as keyof typeof domainErrorKeys;
      const keys = domainErrorKeys[errorCodeKey] || domainErrorKeys.UNKNOWN_ERROR;

      this.toastService.show(error.message ?? this.languageService.translate(keys.message), {
        severity: 'error',
        title: error.title ?? this.languageService.translate(keys.title),
      });
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
