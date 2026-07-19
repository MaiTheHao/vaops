import { Injectable, inject, signal } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { TranslateKeyType } from '../const/translate-key.const';

type SupportedLangCode = 'vi' | 'en';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private readonly translator = inject(TranslateService);

  readonly currentLang = signal<SupportedLangCode>('vi');

  constructor() {
    this.translator.setFallbackLang('vi');
    const saved = (localStorage.getItem('lang') || 'vi') as SupportedLangCode;
    this.currentLang.set(saved);
    this.translator.use(saved);
  }

  switchLang(lang: SupportedLangCode): void {
    this.currentLang.set(lang);
    this.translator.use(lang);
    localStorage.setItem('lang', lang);
  }

  translate(key: TranslateKeyType): string {
    return this.translator.instant(key);
  }
}
