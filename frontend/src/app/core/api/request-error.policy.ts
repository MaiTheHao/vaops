import { HttpContext, HttpContextToken } from '@angular/common/http';

// Token to skip emitting errors to DomainErrorBus for custom handling.
export const SKIP_GLOBAL_ERROR_EMISSION = new HttpContextToken<boolean>(() => false);

// Pre-built context to bypass global error emission.
export const BYPASS_GLOBAL_ERROR_CONTEXT = new HttpContext().set(SKIP_GLOBAL_ERROR_EMISSION, true);

// Helper to apply the bypass token to a new or existing HttpContext.
export function withBypassGlobalError(context = new HttpContext()): HttpContext {
  return context.set(SKIP_GLOBAL_ERROR_EMISSION, true);
}
