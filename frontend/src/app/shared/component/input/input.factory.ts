import { Injectable } from '@angular/core';
import { InputDto, InputCssDto, InputIconDto } from './input.dto';

const BASE_CSS: InputCssDto = {
  container: 'flex flex-col gap-2',
  label: 'font-label-technical text-label-technical text-primary uppercase',
  inputWrapper: 'relative',
  input: 'w-full pl-11 pr-4 py-3 bg-surface border border-outline-variant rounded-none font-body-md text-on-surface input-focus-ring transition-all disabled:opacity-50',
  iconWrapper: 'absolute left-3 top-1/2 -translate-y-1/2 text-outline flex items-center',
  requiredStar: 'text-error',
};

const PASSWORD_CSS: InputCssDto = {
  ...BASE_CSS,
  input: 'w-full pl-11 pr-12 py-3 bg-surface border border-outline-variant rounded-none font-body-md text-on-surface input-focus-ring transition-all disabled:opacity-50',
};

@Injectable({ providedIn: 'root' })
export class InputFactoryService {
  createConfig(overrides?: Partial<InputDto>): InputDto {
    return {
      type: 'text',
      label: '',
      placeholder: '',
      required: false,
      ...overrides,
      css: { ...BASE_CSS, ...overrides?.css },
    };
  }

  createTextConfig(icon?: InputIconDto, overrides?: Partial<InputDto>): InputDto {
    return this.createConfig({ type: 'text', icon, ...overrides });
  }

  createPasswordConfig(icon?: InputIconDto, overrides?: Partial<InputDto>): InputDto {
    return this.createConfig({
      type: 'password',
      placeholder: '••••••••',
      icon,
      css: PASSWORD_CSS,
      ...overrides,
    });
  }

  createUrlConfig(icon?: InputIconDto, overrides?: Partial<InputDto>): InputDto {
    return this.createConfig({ type: 'url', icon, ...overrides });
  }
}
