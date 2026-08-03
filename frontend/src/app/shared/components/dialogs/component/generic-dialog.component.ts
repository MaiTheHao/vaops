import { Component, inject } from '@angular/core';
import { DialogRef, DIALOG_DATA } from '@angular/cdk/dialog';
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
  imports: [LucideCircleAlert, LucideCircleHelp, LucideInfo, LucideX],
  template: `
    <div class="bg-surface-container-lowest border border-outline-variant p-6 shadow-sm max-w-[27.5rem] w-full font-body-md text-on-background">
      <!-- Header -->
      <div class="flex justify-between items-center pb-4 border-b border-outline-variant mb-4">
        <div class="flex items-center gap-2">
          @if (data.type === 'confirm') {
            <svg lucideCircleHelp class="size-5 text-secondary stroke-[2] shrink-0"></svg>
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
