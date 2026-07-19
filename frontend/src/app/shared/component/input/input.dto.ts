import { Type } from '@angular/core';

export interface InputIconDto {
  component: Type<any>;
  position: 'left' | 'right';
  cssClass?: string;
}

export interface InputCssDto {
  container?: string;
  label?: string;
  inputWrapper?: string;
  input?: string;
  iconWrapper?: string;
  requiredStar?: string;
}

export interface InputDto {
  type: string;
  label: string;
  placeholder: string;
  required: boolean;
  icon?: InputIconDto;
  css?: InputCssDto;
}
