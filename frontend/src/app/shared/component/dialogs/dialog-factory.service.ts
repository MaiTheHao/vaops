import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CdkDialogWrapper } from './core/cdk-dialog.wrapper';
import { DialogStrategy } from './strategy/dialog.strategy';
import { ConfirmStrategy } from './strategy/confirm.strategy';
import { InfoStrategy } from './strategy/info.strategy';
import { ErrorStrategy } from './strategy/error.strategy';

export type DialogType = 'confirm' | 'info' | 'error';

@Injectable({ providedIn: 'root' })
export class DialogFactoryService {
  private readonly wrapper = inject(CdkDialogWrapper);

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
}
