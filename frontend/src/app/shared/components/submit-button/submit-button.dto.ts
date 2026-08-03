export interface SubmitButtonCssDto {
  button?: string;
  spinner?: string;
}

export interface SubmitButtonDto {
  label: string;
  loadingLabel: string;
  css?: SubmitButtonCssDto;
}
