import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpErrorResponse,
} from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, filter, switchMap, take } from 'rxjs/operators';
import { ErrorCode } from '../../constants/error-code';
import { ApiErrorMapper } from '../../mappers/api-error.mapper';
import { DomainErrorBusService } from '../../services/domain-error-bus.service';
import { AuthApiService } from '../auth.api.service';
import { SKIP_ERROR_EMISSION } from '../http-context.tokens';
import { ApiError } from '../../../shared/models/api-error.model';
import { DomainError, ErrorActionType } from '../../../shared/models/domain-error.model';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
  private isRefreshing = false;
  private redirectEmitted = false;
  private readonly refreshState = new BehaviorSubject<boolean | null>(null);

  constructor(
    private readonly mapper: ApiErrorMapper,
    private readonly errorBus: DomainErrorBusService,
    private readonly authApi: AuthApiService,
  ) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((errorResponse: HttpErrorResponse) => {
        if (req.context.get(SKIP_ERROR_EMISSION)) {
          // Auth endpoints: auth.service owns error presentation. Still throw so callers can react.
          return throwError(() => errorResponse);
        }

        const apiError: ApiError = this.isApiErrorBody(errorResponse.error)
          ? (errorResponse.error as ApiError)
          : this.fallbackApiError(errorResponse);

        if (apiError.code === ErrorCode.TOKEN_EXPIRED) {
          return this.handleTokenExpired(req, next, apiError);
        }

        const domainError = this.mapper.mapToDomain(apiError);
        this.errorBus.emit(domainError);

        return throwError(() => domainError);
      }),
    );
  }

  private handleTokenExpired(
    req: HttpRequest<unknown>,
    next: HttpHandler,
    apiError: ApiError,
  ): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.redirectEmitted = false;
      this.refreshState.next(null);

      return this.authApi.refresh().pipe(
        catchError(() => {
          this.isRefreshing = false;
          this.refreshState.next(false);
          return throwError(() => this.buildSessionExpiredError(apiError));
        }),
        switchMap(() => {
          this.isRefreshing = false;
          this.refreshState.next(true);
          return next.handle(req);
        }),
      );
    }

    return this.refreshState.pipe(
      filter((state) => state !== null),
      take(1),
      switchMap((state) =>
        state ? next.handle(req) : throwError(() => this.buildSessionExpiredError(apiError)),
      ),
    );
  }

  private buildSessionExpiredError(apiError: ApiError): DomainError {
    const domainError = this.mapper.mapToDomain(apiError);

    if (!this.redirectEmitted) {
      this.redirectEmitted = true;
      this.errorBus.emit(domainError);
    }

    return {
      ...domainError,
      actionType: ErrorActionType.SILENT,
    };
  }

  private isApiErrorBody(body: unknown): body is ApiError {
    return !!body && typeof body === 'object' && 'code' in body && 'message' in body;
  }

  private fallbackApiError(response: HttpErrorResponse): ApiError {
    const code = this.mapper.mapStatusCode(response.status);
    return {
      timestamp: new Date().toISOString(),
      status: response.status === 0 ? 0 : response.status || 500,
      code,
      message: this.fallbackMessage(code),
      path: response.url || '',
      requestId: response.headers?.get('X-Request-Id') || 'UNKNOWN',
    };
  }

  private fallbackMessage(code: ErrorCode): string {
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
}
