import { Component, input, model } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgComponentOutlet } from '@angular/common';
import { InputDto } from './input.dto';

@Component({
  selector: 'app-input',
  standalone: true,
  imports: [FormsModule, NgComponentOutlet],
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
          [type]="config().type"
          [id]="name()"
          [name]="name()"
          [ngModel]="value()"
          (ngModelChange)="value.set($event)"
          [placeholder]="config().placeholder"
          [required]="config().required"
          [disabled]="disabled()"
        />

        @if (config().icon?.position === 'right') {
          <div [class]="config().css?.iconWrapper ?? ''">
            <ng-container *ngComponentOutlet="config().icon!.component" />
          </div>
        }
      </div>
    </div>
  `,
})
export class InputComponent {
  readonly config = input.required<InputDto>();
  readonly name = input<string>('');
  readonly value = model<string>('');
  readonly disabled = input<boolean>(false);
}
