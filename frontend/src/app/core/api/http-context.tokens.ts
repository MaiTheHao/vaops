import { HttpContextToken } from '@angular/common/http';

/** Set on auth API requests so the HttpErrorInterceptor skips error-bus emission (auth.service is the sole auth emitter). */
export const SKIP_ERROR_EMISSION = new HttpContextToken<boolean>(() => false);
