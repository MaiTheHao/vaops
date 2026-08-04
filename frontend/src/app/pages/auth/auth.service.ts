import { inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs/operators';
import { AuthApiService } from '../../core/api/auth.api.service';
import { EventBusService } from '../../core/services/event-bus.service';
import { AppEventKey } from '../../core/constants/app-event.const';
import { DomainErrorBusService } from '../../core/services/domain-error-bus.service';
import { ErrorCode } from '../../core/constants/error-code';
import { ErrorActionType, ErrorSeverity } from '../../shared/models/domain-error.model';
import { LanguageService } from '../../core/services/language.service';
import { TranslateKey } from '../../core/constants/translate-key.const';

@Injectable()
export class AuthService {
  readonly loading = signal(false);
  private readonly authApi = inject(AuthApiService);
  private readonly eventBus = inject(EventBusService);
  private readonly domainErrorBus = inject(DomainErrorBusService);
  private readonly languageService = inject(LanguageService);

  login(accountName: string, password: string): void {
    this.loading.set(true);
    this.authApi
      .login({ accountName, password })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.eventBus.publish(AppEventKey.LOGIN_SUCCESS);
        },
        error: (error) => {
          this.domainErrorBus.emit({
            code: ErrorCode.AUTHENTICATION_FAILED,
            title: this.languageService.translate(TranslateKey.auth.dialog.loginError),
            message: this.languageService.translate(TranslateKey.auth.dialog.loginFailedMessage),
            httpStatus: 0,
            severity: ErrorSeverity.ERROR,
            actionType: ErrorActionType.DIALOG,
            retryable: false,
            requestId: `CLIENT-${Date.now()}`,
            originalError: error instanceof Error ? error : new Error(String(error)),
          });
        },
      });
  }

  register(accountName: string, password: string, displayName: string, avatarUrl?: string): void {
    this.loading.set(true);
    this.authApi
      .register({
        accountName,
        password,
        displayName,
        avatarUrl: avatarUrl || undefined,
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        error: (error) => {
          this.domainErrorBus.emit({
            code: ErrorCode.VALIDATION_FAILED,
            title: this.languageService.translate(TranslateKey.auth.dialog.registerError),
            message: this.languageService.translate(TranslateKey.auth.dialog.registerFailedMessage),
            httpStatus: 0,
            severity: ErrorSeverity.ERROR,
            actionType: ErrorActionType.DIALOG,
            retryable: false,
            requestId: `CLIENT-${Date.now()}`,
            originalError: error instanceof Error ? error : new Error(String(error)),
          });
        },
      });
  }

  logout(): void {
    this.loading.set(true);
    this.authApi
      .logout()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.eventBus.publish(AppEventKey.LOGOUT);
        },
        error: () => {
          this.eventBus.publish(AppEventKey.LOGOUT);
        },
      });
  }
}
