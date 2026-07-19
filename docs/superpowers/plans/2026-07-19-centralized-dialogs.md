# Centralized Dialogs Framework Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a robust, centralized dialog system for VAOPS using the Factory + Strategy + Wrapper patterns to encapsulate `@angular/cdk/dialog`.
**Architecture:** Use a `CdkDialogWrapper` to shield third-party code, a family of `DialogStrategy` implementations to format options, a generic component with Tailwind classes for layout, and a `DialogFactoryService` for client interactions.
**Tech Stack:** Angular 21, `@angular/cdk/dialog`, Tailwind CSS v4, `@lucide/angular`.

## Global Constraints
* Code layout: Use `frontend/src/app/shared/component/dialogs/` as the root.
* All folder names must be singular: `core`, `strategy`, `component`.
* Aesthetic: Strict Industrial Theme (square corners `rounded-none`, `border-outline-variant`, `shadow-sm`, `Outfit` font).

---

### Task 1: Add Custom Backdrop Styling

**Files:**
* Modify: `frontend/src/styles.css`

**Interfaces:**
* Produces: CSS class `.custom-dialog-backdrop`

- [ ] **Step 1: Append styling to `styles.css`**

Add the backdrop style class:
```css
/* Custom dialog backdrop layout */
.custom-dialog-backdrop {
  background-color: rgba(27, 28, 30, 0.45);
  backdrop-filter: blur(2px);
  position: fixed;
  inset: 0;
  z-index: 50;
}
```

- [ ] **Step 2: Commit styling**
```bash
git add frontend/src/styles.css
git commit -m "style: add custom-dialog-backdrop to global styles"
```

---

### Task 2: Implement CdkDialogWrapper

**Files:**
* Create: `frontend/src/app/shared/component/dialogs/core/cdk-dialog.wrapper.ts`

**Interfaces:**
* Consumes: `@angular/cdk/dialog`
* Produces: `CdkDialogWrapper` class with `open<R = any>(component: Type<any>, config: { data: any, width?: string, disableClose?: boolean })` returning `Observable<R | undefined>`.

- [ ] **Step 1: Write `cdk-dialog.wrapper.ts`**

```typescript
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
      width: config.width || '440px',
      disableClose: config.disableClose ?? true,
      data: config.data,
      backdropClass: 'custom-dialog-backdrop'
    });
    return dialogRef.closed;
  }
}
```

- [ ] **Step 2: Commit wrapper**
```bash
git add frontend/src/app/shared/component/dialogs/core/cdk-dialog.wrapper.ts
git commit -m "feat(dialog): implement CdkDialogWrapper service"
```

---

### Task 3: Establish DialogStrategy Interface and Concrete Strategies

**Files:**
* Create: `frontend/src/app/shared/component/dialogs/strategy/dialog.strategy.ts`
* Create: `frontend/src/app/shared/component/dialogs/strategy/confirm.strategy.ts`
* Create: `frontend/src/app/shared/component/dialogs/strategy/info.strategy.ts`
* Create: `frontend/src/app/shared/component/dialogs/strategy/error.strategy.ts`

**Interfaces:**
* Consumes: `CdkDialogWrapper`, `GenericDialogComponent`
* Produces: `DialogStrategy` interface and its concrete subclasses (`ConfirmStrategy`, `InfoStrategy`, `ErrorStrategy`).

- [ ] **Step 1: Create `dialog.strategy.ts`**

```typescript
import { Observable } from 'rxjs';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';

export interface DialogStrategy<T = any> {
  execute(wrapper: CdkDialogWrapper, title: string, message: string, extra?: any): Observable<T>;
}
```

- [ ] **Step 2: Create `confirm.strategy.ts`**

```typescript
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
```

- [ ] **Step 3: Create `info.strategy.ts`**

```typescript
import { map, Observable } from 'rxjs';
import { DialogStrategy } from './dialog.strategy';
import { CdkDialogWrapper } from '../core/cdk-dialog.wrapper';
import { GenericDialogComponent } from '../component/generic-dialog.component';

export class InfoStrategy implements DialogStrategy<void> {
  execute(wrapper: CdkDialogWrapper, title: string, message: string): Observable<void> {
    return wrapper.open<void>(GenericDialogComponent, {
      width: '440px',
      data: { title, message, type: 'info' }
    }).pipe(map(() => undefined as void));
  }
}
```

- [ ] **Step 4: Create `error.strategy.ts`**

```typescript
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
```

- [ ] **Step 5: Commit strategies**
```bash
git add frontend/src/app/shared/component/dialogs/strategy/
git commit -m "feat(dialog): add strategy interfaces and concrete strategies"
```

---

### Task 4: Implement GenericDialogComponent UI

**Files:**
* Create: `frontend/src/app/shared/component/dialogs/component/generic-dialog.component.ts`

**Interfaces:**
* Consumes: `DIALOG_DATA`, `DialogRef`
* Produces: `GenericDialogComponent` component rendering UI elements.

- [ ] **Step 1: Write `generic-dialog.component.ts`**

```typescript
import { Component, inject } from '@angular/core';
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog';
import { NgClass } from '@angular/common';
import { LucideCircleAlert, LucideCircleHelp, LucideInfo, LucideX } from '@lucide/angular';

export interface DialogDataPayload {
  title: string;
  message: string;
  type: 'confirm' | 'info' | 'error';
  confirmText?: string;
  cancelText?: string;
}

@Component({
  standalone: true,
  selector: 'app-generic-dialog',
  imports: [NgClass, LucideCircleAlert, LucideCircleHelp, LucideInfo, LucideX],
  template: `
    <div class="bg-surface-container-lowest border border-outline-variant p-6 shadow-sm max-w-[440px] w-full font-body-md text-on-background">
      <!-- Header -->
      <div class="flex justify-between items-center pb-4 border-b border-outline-variant mb-4">
        <div class="flex items-center gap-2">
          @if (data.type === 'confirm') {
            <svg lucideCircleHelp class="size-5 text-secondary-container stroke-[2] shrink-0"></svg>
          } @else if (data.type === 'info') {
            <svg lucideInfo class="size-5 text-primary stroke-[2] shrink-0"></svg>
          } @else if (data.type === 'error') {
            <svg lucideCircleAlert class="size-5 text-error stroke-[2] shrink-0"></svg>
          }
          <h3 class="font-headline-md text-base font-bold text-primary uppercase tracking-tight">{{ data.title }}</h3>
        </div>
        <button (click)="dialogRef.close(false)" class="text-outline hover:text-primary transition-colors cursor-pointer bg-transparent border-none p-0 flex items-center justify-center" type="button">
          <svg lucideX class="size-5"></svg>
        </button>
      </div>

      <!-- Body -->
      <div class="mb-6">
        @if (data.type === 'error') {
          <div class="bg-error-container/20 border border-error/20 p-4 text-on-error-container text-sm leading-relaxed mb-2">
            {{ data.message }}
          </div>
        } @else {
          <p class="text-on-secondary-container text-sm leading-relaxed">{{ data.message }}</p>
        }
      </div>

      <!-- Actions -->
      <div class="flex justify-end gap-3 pt-4 border-t border-outline-variant">
        @if (data.type === 'confirm') {
          <button 
            (click)="dialogRef.close(false)" 
            class="border border-outline-variant text-secondary hover:text-primary bg-surface py-2 px-4 uppercase font-button tracking-widest text-xs transition-all cursor-pointer"
            type="button"
          >
            {{ data.cancelText || 'Hủy' }}
          </button>
          <button 
            (click)="dialogRef.close(true)" 
            class="bg-primary-container text-white py-2 px-4 uppercase font-button tracking-widest text-xs hover:bg-primary transition-all active:scale-[0.98] cursor-pointer"
            type="button"
          >
            {{ data.confirmText || 'Xác nhận' }}
          </button>
        } @else {
          <button 
            (click)="dialogRef.close(true)" 
            class="bg-primary-container text-white py-2 px-6 uppercase font-button tracking-widest text-xs hover:bg-primary transition-all active:scale-[0.98] cursor-pointer"
            type="button"
          >
            Đóng
          </button>
        }
      </div>
    </div>
  `
})
export class GenericDialogComponent {
  readonly dialogRef = inject(DialogRef);
  readonly data = inject<DialogDataPayload>(DIALOG_DATA);
}
```

- [ ] **Step 2: Commit generic dialog UI**
```bash
git add frontend/src/app/shared/component/dialogs/component/generic-dialog.component.ts
git commit -m "feat(dialog): implement GenericDialogComponent UI"
```

---

### Task 5: Implement DialogFactoryService

**Files:**
* Create: `frontend/src/app/shared/component/dialogs/dialog-factory.service.ts`

**Interfaces:**
* Consumes: `CdkDialogWrapper`, `ConfirmStrategy`, `InfoStrategy`, `ErrorStrategy`
* Produces: `DialogFactoryService` with method `open(type: 'confirm' | 'info' | 'error', title: string, message: string, extra?: any): Observable<any>`

- [ ] **Step 1: Write `dialog-factory.service.ts`**

```typescript
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
```

- [ ] **Step 2: Commit Factory**
```bash
git add frontend/src/app/shared/component/dialogs/dialog-factory.service.ts
git commit -m "feat(dialog): implement DialogFactoryService strategy mapping"
```

---

### Task 6: Verify and Build Framework

**Files:**
* Modify: `frontend/src/app/app.config.ts` (to provide `DialogModule` or similar if required)
* Modify: `frontend/src/app/page/auth/auth.component.ts` (temporary trigger verification)
* Modify: `frontend/src/app/page/auth/auth.component.html` (temporary button triggers)

- [ ] **Step 1: Update `app.config.ts` to import `DialogModule` providers**

Open `frontend/src/app/app.config.ts` and ensure dialog configuration providers are supplied:
```typescript
import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { DialogModule } from '@angular/cdk/dialog';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    importProvidersFrom(DialogModule),
    provideTranslateService({
      loader: provideTranslateHttpLoader({
        prefix: './assets/i18n/',
        suffix: '.json'
      }),
      lang: 'vi',
      fallbackLang: 'vi'
    })
  ]
};
```

- [ ] **Step 2: Run dev/build verification**

Run build checks:
```bash
pnpm run build
```
Verify build succeeds.

- [ ] **Step 3: Commit verification and initial setup**
```bash
git add frontend/src/app/app.config.ts
git commit -m "chore(dialog): provide DialogModule in application config"
```
