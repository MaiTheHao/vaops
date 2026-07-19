import { map, Observable } from 'rxjs';
import { DialogStrategy } from './dialog.strategy';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';
import { GenericDialogComponent } from '../component/generic-dialog.component';

export class ErrorStrategy implements DialogStrategy<void> {
  execute(wrapper: CdkDialogWrapper, title: string, message: string): Observable<void> {
    return wrapper.open<void>(GenericDialogComponent, {
      width: '440px',
      data: { title, message, type: 'error' }
    }).pipe(map(() => undefined as void));
  }
}
