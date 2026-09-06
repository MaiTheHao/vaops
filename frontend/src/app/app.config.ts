import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners, provideAppInitializer, inject, ErrorHandler } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { DialogModule } from '@angular/cdk/dialog';

import { routes } from './app.routes';
import { EventBusService } from './core/services/event-bus.service';
import { AppEventKey } from './core/constants/app-event.const';
import { GlobalErrorHandler } from './core/error/handlers/global-error-handler';
import { HttpErrorInterceptor } from './core/api/interceptors/http-error.interceptor';
import { DialogErrorListener } from './core/error/listeners/dialog-error.listener';
import { RedirectErrorListener } from './core/error/listeners/redirect-error.listener';
import { SilentErrorListener } from './core/error/listeners/silent-error.listener';

import { AppConfigService } from './core/services/app-config.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    importProvidersFrom(DialogModule),
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    { provide: HTTP_INTERCEPTORS, useClass: HttpErrorInterceptor, multi: true },
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json'
      }),
      lang: 'vi',
      fallbackLang: 'vi'
    }),
    provideAppInitializer(() => {
      const configService = inject(AppConfigService);
      return configService.loadConfig();
    }),
    provideAppInitializer(() => {
      const eventBusService = inject(EventBusService);
      eventBusService.publish(AppEventKey.APP_INIT);
    }),
    provideAppInitializer(() => {
      inject(DialogErrorListener);
      inject(RedirectErrorListener);
      inject(SilentErrorListener);
    })
  ]
};
