import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CdkDialogWrapper } from './core/cdk-dialog.wrapper';
import { DialogStrategy } from './strategy/dialog.strategy';
import { ConfirmStrategy } from './strategy/confirm.strategy';
import { InfoStrategy } from './strategy/info.strategy';
import { ErrorStrategy } from './strategy/error.strategy';
import { DomainError, ErrorSeverity } from '../../models/domain-error.model';
import { LanguageService } from '../../../core/services/language.service';
import { TranslateKey } from '../../../core/constants/translate-key.const';

export type DialogType = 'confirm' | 'info' | 'error';

@Injectable({ providedIn: 'root' })
export class DialogFactoryService {
  private readonly wrapper = inject(CdkDialogWrapper);
  private readonly languageService = inject(LanguageService);

  private readonly strategies: Record<DialogType, DialogStrategy> = {
    confirm: new ConfirmStrategy(),
    info: new InfoStrategy(),
    error: new ErrorStrategy()
  };

  open(type: DialogType, title: string, message: string, extra?: any): Observable<any> {
    const strategy = this.strategies[type];
    if (!strategy) {
      throw new Error(`Dialog type "${type}" is not supported by DialogFactoryService.`);
    }
    return strategy.execute(this.wrapper, title, message, extra);
  }

  openDomainError(error: DomainError): Observable<any> {
    const type: DialogType =
      error.severity === ErrorSeverity.ERROR || error.severity === ErrorSeverity.CRITICAL
        ? 'error'
        : 'info';

    const domainErrorKeys = TranslateKey.error.domain;
    const errorCodeKey = error.code as keyof typeof domainErrorKeys;
    const keys = domainErrorKeys[errorCodeKey] || domainErrorKeys.UNKNOWN_ERROR;

    const title = error.title ?? this.languageService.translate(keys.title);
    const message = error.message ?? this.languageService.translate(keys.message);

    return this.open(type, title, message, { domainError: error });
  }
}
