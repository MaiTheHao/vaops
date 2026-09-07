import { inject } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { ErrorCode } from '../../constants/error-code';
import { ApiErrorMapper } from '../../mappers/api-error.mapper';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { AuthSessionService } from '../../services/auth-session.service';
import { SKIP_GLOBAL_ERROR_EMISSION } from '../request-error.policy';
import { ApiError } from '../../../shared/models/api-error.model';
import { DomainError, ErrorActionType } from '../../../shared/models/domain-error.model';

function isApiErrorBody(body: unknown): body is ApiError {
  return !!body && typeof body === 'object' && 'code' in body && 'message' in body;
}

function fallbackMessage(code: ErrorCode): string {
  switch (code) {
    case ErrorCode.TIMEOUT:
      return 'Connection lost. Please try again.';
    case ErrorCode.AUTHENTICATION_FAILED:
      return 'Authentication failed. Please sign in again.';
    case ErrorCode.ACCESS_DENIED:
      return 'You do not have permission to perform this action.';
    case ErrorCode.RESOURCE_NOT_FOUND:
      return 'The requested resource was not found.';
    case ErrorCode.UNKNOWN_ERROR:
      return 'An unexpected error occurred.';
    default:
      return 'An internal error occurred. Please try again later.';
  }
}

function fallbackApiError(response: HttpErrorResponse, mapper: ApiErrorMapper): ApiError {
  const code = mapper.mapStatusCode(response.status);
  return {
    timestamp: new Date().toISOString(),
    status: response.status === 0 ? 0 : response.status || 500,
    code,
    message: fallbackMessage(code),
    path: response.url || '',
    requestId: response.headers?.get('X-Request-Id') || 'UNKNOWN',
  };
}

function buildSessionExpiredError(
  apiError: ApiError,
  mapper: ApiErrorMapper,
  errorBus: DomainErrorBusService,
  session: AuthSessionService,
): DomainError {
  const domainError = mapper.mapToDomain(apiError);

  if (!session.isRedirectEmitted()) {
    session.markRedirectEmitted();
    errorBus.emit(domainError);
  }

  return {
    ...domainError,
    actionType: ErrorActionType.SILENT,
  };
}

export const httpErrorInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const mapper = inject(ApiErrorMapper);
  const errorBus = inject(DomainErrorBusService);
  const session = inject(AuthSessionService);

  return next(req).pipe(
    catchError((errorResponse: HttpErrorResponse) => {
      if (req.context.get(SKIP_GLOBAL_ERROR_EMISSION)) {
        return throwError(() => errorResponse);
      }

      const apiError: ApiError = isApiErrorBody(errorResponse.error)
        ? errorResponse.error
        : fallbackApiError(errorResponse, mapper);

      if (apiError.code === ErrorCode.TOKEN_EXPIRED) {
        return session.refreshToken().pipe(
          switchMap(() => next(req)),
          catchError(() =>
            throwError(() => buildSessionExpiredError(apiError, mapper, errorBus, session)),
          ),
        );
      }

      const domainError = mapper.mapToDomain(apiError);
      errorBus.emit(domainError);

      return throwError(() => domainError);
    }),
  );
};
