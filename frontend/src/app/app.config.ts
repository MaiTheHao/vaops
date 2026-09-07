import {
  ApplicationConfig,
  ErrorHandler,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';

import { routes } from './app.routes';
import { EventBusService } from './core/services/event-bus.service';
import { AppEventKey } from './core/constants/app-event.const';
import { GlobalErrorHandler } from './core/error/handlers/global-error-handler';
import { httpErrorInterceptor } from './core/api/interceptors/http-error.interceptor';
import { DialogErrorListener } from './core/error/listeners/dialog-error.listener';
import { RedirectErrorListener } from './core/error/listeners/redirect-error.listener';
import { SilentErrorListener } from './core/error/listeners/silent-error.listener';
import { AppConfigService } from './core/services/app-config.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([httpErrorInterceptor])),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json',
      }),
      lang: 'vi',
      fallbackLang: 'vi',
    }),
    provideAppInitializer(async () => {
      inject(DialogErrorListener);
      inject(RedirectErrorListener);
      inject(SilentErrorListener);

      const configService = inject(AppConfigService);
      await configService.loadConfig();

      const eventBusService = inject(EventBusService);
      eventBusService.publish(AppEventKey.APP_INIT);
    }),
  ],
};
