import { map, Observable } from 'rxjs';
import { DialogStrategy } from './dialog.strategy';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';
import { GenericDialogComponent } from '../component/generic-dialog.component';

export class ConfirmStrategy implements DialogStrategy<boolean> {
  execute(
    wrapper: CdkDialogWrapper,
    title: string,
    message: string,
    extra?: { confirmText?: string; cancelText?: string }
  ): Observable<boolean> {
    return wrapper.open<boolean>(GenericDialogComponent, {
      width: '440px',
      data: { title, message, type: 'confirm', ...extra }
    }).pipe(map(result => !!result));
  }
}
