import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { DomainError, ErrorActionType, ErrorSeverity } from '../../../shared/models/domain-error.model';
import { ErrorCode } from '../../constants/error-code';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  constructor(private readonly injector: Injector) {}

  handleError(error: unknown): void {
    if (this.isDomainError(error)) return;

    const errorBus = this.injector.get(DomainErrorBusService);

    const clientError: DomainError = {
      code: ErrorCode.UNKNOWN_ERROR,
      httpStatus: 0,
      severity: ErrorSeverity.ERROR,
      actionType: ErrorActionType.DIALOG,
      retryable: false,
      requestId: `CLIENT-${Date.now()}`,
      originalError: error instanceof Error ? error : new Error(String(error))
    };

    errorBus.emit(clientError);
  }

  private isDomainError(error: unknown): boolean {
    return !!error && typeof error === 'object' && 'actionType' in error && 'code' in error;
  }
}
