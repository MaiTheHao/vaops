import { inject, Injectable, Type } from '@angular/core';
import { Dialog } from '@angular/cdk/dialog';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CdkDialogWrapper {
  private readonly cdkDialog = inject(Dialog);

  open<R = any>(
    component: Type<any>,
    config: { data: any; width?: string; disableClose?: boolean }
  ): Observable<R | undefined> {
    const dialogRef = this.cdkDialog.open<R>(component, {
      width: config.width || '27.5rem',
      disableClose: config.disableClose ?? true,
      data: config.data,
      backdropClass: 'custom-dialog-backdrop'
    });
    return dialogRef.closed;
  }
}
