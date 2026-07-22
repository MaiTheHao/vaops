import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners, provideAppInitializer, inject } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { DialogModule } from '@angular/cdk/dialog';

import { routes } from './app.routes';
import { EventManager } from './shared/service/event-manager.service';
import { AppEventKey } from './shared/const/app-event.const';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    importProvidersFrom(DialogModule),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json'
      }),
      lang: 'vi',
      fallbackLang: 'vi'
    }),
    provideAppInitializer(() => {
      const eventManager = inject(EventManager);
      eventManager.publish(AppEventKey.APP_INIT);
    })
  ]
};
