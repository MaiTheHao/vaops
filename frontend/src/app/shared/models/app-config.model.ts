export interface AppConfig {
  readonly apiUrl: string;
  readonly appTitle?: string;
  readonly featureFlags?: {
    readonly [key: string]: boolean | undefined;
  };
}
