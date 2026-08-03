import { Observable } from 'rxjs';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';

export interface DialogStrategy<T = any> {
  execute(wrapper: CdkDialogWrapper, title: string, message: string, extra?: any): Observable<T>;
}
