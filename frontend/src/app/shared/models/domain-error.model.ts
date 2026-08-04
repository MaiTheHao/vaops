import { ApiError } from './api-error.model';
import { ErrorCode } from '../../core/constants/error-code';

export enum ErrorSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  ERROR = 'ERROR',
  CRITICAL = 'CRITICAL'
}

export enum ErrorActionType {
  DIALOG = 'DIALOG',
  REDIRECT = 'REDIRECT',
  SILENT = 'SILENT'
}

export interface DomainError {
  readonly code: ErrorCode | string;
  readonly httpStatus: number;
  readonly title?: string;
  readonly message?: string;
  readonly severity: ErrorSeverity;
  readonly actionType: ErrorActionType;
  readonly retryable: boolean;
  readonly requestId: string;
  readonly redirectUrl?: string;
  readonly originalError?: ApiError | Error;
}

