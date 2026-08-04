import { ErrorCode } from '../../core/constants/error-code';

export interface ApiError {
  readonly timestamp: string;
  readonly status: number;
  readonly code: ErrorCode | string;
  readonly message: string;
  readonly path: string;
  readonly requestId: string;
  readonly details?: Record<string, unknown>;
}
