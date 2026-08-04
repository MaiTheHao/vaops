import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { DomainError, ErrorActionType } from '../../shared/models/domain-error.model';

@Injectable({
  providedIn: 'root',
})
export class DomainErrorBusService {
  private readonly errorSubject = new Subject<DomainError>();

  public readonly error$: Observable<DomainError> = this.errorSubject.asObservable();

  public ofType(actionType: ErrorActionType): Observable<DomainError> {
    return this.error$.pipe(filter((error) => error.actionType === actionType));
  }

  public emit(error: DomainError): void {
    console.error(`[ErrorBus] [${error.code}] [ReqId: ${error.requestId}]`, error);
    this.errorSubject.next(error);
  }
}
