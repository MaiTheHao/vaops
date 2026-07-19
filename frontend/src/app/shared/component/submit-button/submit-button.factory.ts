import { Injectable } from '@angular/core';
import { SubmitButtonDto, SubmitButtonCssDto } from './submit-button.dto';

const BTN_CSS: SubmitButtonCssDto = {
  button: 'w-full bg-primary-container text-white font-button text-button py-4 uppercase tracking-widest hover:bg-primary transition-all active:scale-[0.98] border border-transparent cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2',
  spinner: 'animate-spin size-5 shrink-0',
};

@Injectable({ providedIn: 'root' })
export class ButtonFactoryService {
  createConfig(overrides?: Partial<SubmitButtonDto>): SubmitButtonDto {
    return {
      label: 'Submit',
      loadingLabel: 'Đang xử lý...',
      ...overrides,
      css: { ...BTN_CSS, ...overrides?.css },
    };
  }
}
