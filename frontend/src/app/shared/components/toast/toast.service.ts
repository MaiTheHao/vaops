import { Component, inject, Injectable, OnDestroy, signal } from '@angular/core';
import { Overlay, OverlayRef } from '@angular/cdk/overlay';
import { ComponentPortal } from '@angular/cdk/portal';
import { LucideCircleAlert, LucideInfo, LucideTriangleAlert, LucideX } from '@lucide/angular';

export type ToastSeverity = 'info' | 'warning' | 'error';

export interface ToastOptions {
  severity?: ToastSeverity;
  durationMs?: number;
  title?: string;
}

export interface ToastItem {
  id: number;
  message: string;
  severity: ToastSeverity;
  durationMs: number;
  title?: string;
}

const DEFAULT_DURATION_MS = 4500;

@Component({
  standalone: true,
  selector: 'app-toast-container',
  imports: [LucideCircleAlert, LucideInfo, LucideTriangleAlert, LucideX],
  template: `
    <div class="flex flex-col items-end gap-3" role="status" aria-live="polite">
      @for (toast of toastService.toasts(); track toast.id) {
        <div [class]="toastCardClass(toast)" (click)="toastService.dismiss(toast.id)">
          <div class="flex items-start gap-3">
            @switch (toast.severity) {
              @case ('error') {
                <svg lucideCircleAlert class="size-5 text-error stroke-[2] shrink-0 mt-0.5"></svg>
              }
              @case ('warning') {
                <svg lucideTriangleAlert class="size-5 text-amber-600 stroke-[2] shrink-0 mt-0.5"></svg>
              }
              @default {
                <svg lucideInfo class="size-5 text-primary stroke-[2] shrink-0 mt-0.5"></svg>
              }
            }
            <div class="flex-1 min-w-0">
              @if (toast.title) {
                <div class="font-button text-xs uppercase tracking-widest text-on-background mb-1">
                  {{ toast.title }}
                </div>
              }
              <div class="text-sm leading-snug text-on-secondary-container break-words">
                {{ toast.message }}
              </div>
            </div>
            <button
              (click)="toastService.dismiss(toast.id); $event.stopPropagation()"
              class="text-outline hover:text-primary transition-colors cursor-pointer bg-transparent border-none p-0 flex items-center justify-center shrink-0"
              type="button"
              aria-label="Đóng"
            >
              <svg lucideX class="size-4"></svg>
            </button>
          </div>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  readonly toastService = inject(ToastService);

  toastCardClass(toast: ToastItem): string {
    const severityBorder: Record<ToastSeverity, string> = {
      info: 'border-l-primary',
      warning: 'border-l-amber-500',
      error: 'border-l-error',
    };
    return (
      'pointer-events-auto w-[22rem] max-w-[calc(100vw-2rem)] rounded-md shadow-lg bg-surface-container-lowest border border-outline-variant border-l-4 p-4 cursor-pointer transition-transform hover:scale-[1.01] ' +
      severityBorder[toast.severity]
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ToastService implements OnDestroy {
  private readonly overlay = inject(Overlay);

  readonly toasts = signal<ToastItem[]>([]);

  private overlayRef: OverlayRef | null = null;
  private nextId = 0;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  show(message: string, options?: ToastOptions): void {
    const item: ToastItem = {
      id: this.nextId++,
      message,
      severity: options?.severity ?? 'info',
      durationMs: options?.durationMs ?? DEFAULT_DURATION_MS,
      title: options?.title,
    };
    this.toasts.update((items) => [...items, item]);
    this.ensureContainer();
    const timer = setTimeout(() => this.dismiss(item.id), item.durationMs);
    this.timers.set(item.id, timer);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this.toasts.update((items) => items.filter((toast) => toast.id !== id));
    if (this.toasts().length === 0) {
      this.disposeOverlay();
    }
  }

  ngOnDestroy(): void {
    this.timers.forEach((timer) => clearTimeout(timer));
    this.timers.clear();
    this.disposeOverlay();
  }

  private ensureContainer(): void {
    if (this.overlayRef) {
      return;
    }
    this.overlayRef = this.overlay.create({
      positionStrategy: this.overlay.position().global().end('16px').top('16px'),
      scrollStrategy: this.overlay.scrollStrategies.reposition(),
    });
    this.overlayRef.attach(new ComponentPortal(ToastContainerComponent));
  }

  private disposeOverlay(): void {
    this.overlayRef?.dispose();
    this.overlayRef = null;
  }
}
