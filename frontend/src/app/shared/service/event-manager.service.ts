import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { AppEvent } from '../const/app-event.const';

@Injectable({
  providedIn: 'root',
})
export class EventManager {
  private bus$ = new Subject<AppEvent>();

  publish<T>(name: string, payload?: T): void {
    this.bus$.next({ name, payload });
  }

  listen<T>(eventNames: string | string[]): Observable<T> {
    const names = Array.isArray(eventNames) ? eventNames : [eventNames];
    return this.bus$.asObservable().pipe(
      filter(event => names.includes(event.name)),
      map(event => event.payload as T)
    );
  }

  listenEvent<T>(eventNames: string | string[]): Observable<AppEvent<T>> {
    const names = Array.isArray(eventNames) ? eventNames : [eventNames];
    return this.bus$.asObservable().pipe(
      filter(event => names.includes(event.name)),
      map(event => event as AppEvent<T>)
    );
  }
}
