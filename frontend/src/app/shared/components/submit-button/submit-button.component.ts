import { Component, input } from '@angular/core';
import { LucideLoaderCircle } from '@lucide/angular';
import { SubmitButtonDto } from './submit-button.dto';

@Component({
  selector: 'app-submit-button',
  standalone: true,
  imports: [LucideLoaderCircle],
  template: `
    <button type="submit" [class]="config().css?.button ?? ''" [disabled]="disabled() || loading()">
      @if (loading()) {
        <svg lucideLoaderCircle [class]="config().css?.spinner ?? ''"></svg>
        <span>{{ config().loadingLabel }}</span>
      } @else {
        <span>{{ config().label }}</span>
      }
    </button>
  `,
})
export class SubmitButtonComponent {
  readonly config = input.required<SubmitButtonDto>();
  readonly loading = input<boolean>(false);
  readonly disabled = input<boolean>(false);
}
