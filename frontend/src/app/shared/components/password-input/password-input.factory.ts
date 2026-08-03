import { Injectable, inject } from '@angular/core';
import { InputFactoryService } from '../input/input.factory';
import { InputIconDto } from '../input/input.dto';
import { PasswordInputDto } from './password-input.dto';

@Injectable({ providedIn: 'root' })
export class PasswordInputFactoryService {
  private readonly inputFactory = inject(InputFactoryService);

  createConfig(icon?: InputIconDto, overrides?: Partial<PasswordInputDto>): PasswordInputDto {
    return {
      ...this.inputFactory.createPasswordConfig(icon, overrides),
      showPassword: false,
      ...overrides,
    };
  }
}
