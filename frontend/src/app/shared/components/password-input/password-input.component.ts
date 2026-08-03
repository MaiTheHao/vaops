import { Component, input, model, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgComponentOutlet } from '@angular/common';
import { LucideEye, LucideEyeOff} from '@lucide/angular';
import { PasswordInputDto } from './password-input.dto';

@Component({
  selector: 'app-password-input',
  standalone: true,
  imports: [FormsModule, NgComponentOutlet, LucideEye, LucideEyeOff],
  template: `
    <div [class]="config().css?.container ?? ''">
      @if (config().label) {
        <label [class]="config().css?.label ?? ''" [for]="name()">
          {{ config().label }}
          @if (config().required) {
            <span [class]="config().css?.requiredStar ?? ''">*</span>
          }
        </label>
      }

      <div [class]="config().css?.inputWrapper ?? 'relative'">
        @if (config().icon?.position === 'left') {
          <div [class]="config().css?.iconWrapper ?? ''">
            <ng-container *ngComponentOutlet="config().icon!.component" />
          </div>
        }

        <input
          [class]="config().css?.input ?? ''"
          [type]="showPassword() ? 'text' : 'password'"
          [id]="name()"
          [name]="name()"
          [ngModel]="value()"
          (ngModelChange)="value.set($event)"
          [placeholder]="config().placeholder"
          [required]="config().required"
          [disabled]="disabled()"
        />

        <button
          type="button"
          (click)="showPassword.set(!showPassword())"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-outline hover:text-primary transition-colors cursor-pointer flex items-center justify-center bg-transparent border-none p-0"
        >
          @if (showPassword()) {
            <svg lucideEyeOff class="size-5"></svg>
          } @else {
            <svg lucideEye class="size-5"></svg>
          }
        </button>
      </div>
    </div>
  `,
})
export class PasswordInputComponent {
  readonly config = input.required<PasswordInputDto>();
  readonly name = input<string>('');
  readonly value = model<string>('');
  readonly disabled = input<boolean>(false);

  readonly showPassword = signal(false);
}
