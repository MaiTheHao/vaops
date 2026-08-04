import { map, Observable } from 'rxjs';
import { DialogStrategy } from './dialog.strategy';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';
import { GenericDialogComponent } from '../component/generic-dialog.component';
import { DomainError } from '../../../models/domain-error.model';

export class ErrorStrategy implements DialogStrategy<void> {
  execute(
    wrapper: CdkDialogWrapper,
    title: string,
    message: string,
    extra?: { domainError?: DomainError },
  ): Observable<void> {
    return wrapper.open<void>(GenericDialogComponent, {
      width: '27.5rem',
      data: { title, message, type: 'error', requestId: extra?.domainError?.requestId },
    }).pipe(map(() => undefined as void));
  }
}
