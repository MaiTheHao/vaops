import { Injectable } from '@angular/core';
import { ErrorCode } from '../constants/error-code';
import { ApiError } from '../../shared/models/api-error.model';
import {
  DomainError,
  ErrorSeverity,
  ErrorActionType,
} from '../../shared/models/domain-error.model';

@Injectable({ providedIn: 'root' })
export class ApiErrorMapper {
  private static readonly STATUS_CODE_MAP: Readonly<Record<number, ErrorCode>> = {
    0: ErrorCode.TIMEOUT,
    401: ErrorCode.AUTHENTICATION_FAILED,
    403: ErrorCode.ACCESS_DENIED,
    404: ErrorCode.RESOURCE_NOT_FOUND,
  };

  public mapToDomain(apiError: ApiError): DomainError {
    const code = this.normalizeCode(apiError.code);

    switch (code) {
      case ErrorCode.VALIDATION_FAILED:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.WARNING,
          actionType: ErrorActionType.DIALOG,
          retryable: false,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.TOKEN_EXPIRED:
      case ErrorCode.AUTHENTICATION_FAILED:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.WARNING,
          actionType: ErrorActionType.REDIRECT,
          redirectUrl: '/auth',
          retryable: true,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.ACCOUNT_LOCKED:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.CRITICAL,
          actionType: ErrorActionType.DIALOG,
          retryable: false,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.ACCESS_DENIED:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.ERROR,
          actionType: ErrorActionType.DIALOG,
          requestId: apiError.requestId,
          retryable: false,
          originalError: apiError,
        };

      case ErrorCode.RESOURCE_NOT_FOUND:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.ERROR,
          actionType: ErrorActionType.DIALOG,
          requestId: apiError.requestId,
          retryable: false,
          originalError: apiError,
        };

      case ErrorCode.CONCURRENCY_CONFLICT:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.WARNING,
          actionType: ErrorActionType.DIALOG,
          retryable: true,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.MALFORMED_REQUEST:
      case ErrorCode.TYPE_MISMATCH:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.ERROR,
          actionType: ErrorActionType.DIALOG,
          retryable: false,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.FILE_SIZE_LIMIT_EXCEEDED:
        return {
          code,
          httpStatus: apiError.status,
          severity: ErrorSeverity.WARNING,
          actionType: ErrorActionType.DIALOG,
          retryable: false,
          requestId: apiError.requestId,
          originalError: apiError,
        };

      case ErrorCode.INTERNAL_ERROR:
      case ErrorCode.EXTERNAL_SERVICE_ERROR:
      default:
        return {
          code: code || ErrorCode.UNKNOWN_ERROR,
          httpStatus: apiError.status || 500,
          severity: ErrorSeverity.ERROR,
          actionType: ErrorActionType.DIALOG,
          retryable: true,
          requestId: apiError.requestId || 'N/A',
          originalError: apiError,
        };
    }
  }

  public mapStatusCode(status: number): ErrorCode {
    if (status >= 500) {
      return ErrorCode.INTERNAL_ERROR;
    }

    const code = ApiErrorMapper.STATUS_CODE_MAP[status];
    if (code !== undefined) {
      return code;
    }

    console.warn(
      `[ApiErrorMapper] Unmapped HTTP status ${status}; mapping to UNKNOWN_ERROR`,
    );
    return ErrorCode.UNKNOWN_ERROR;
  }

  private normalizeCode(code: ErrorCode | string): ErrorCode {
    return Object.values(ErrorCode).includes(code as ErrorCode)
      ? (code as ErrorCode)
      : ErrorCode.UNKNOWN_ERROR;
  }
}
