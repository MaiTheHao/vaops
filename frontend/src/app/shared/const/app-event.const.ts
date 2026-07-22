export interface AppEvent<T = any> {
  name: string;
  payload?: T;
}

export const AppEventKey = {
  APP_INIT: 'APP_INIT',
  LOGIN_SUCCESS: 'LOGIN_SUCCESS',
  PROFILE_CHANGED: 'PROFILE_CHANGED',
  PROFILE_SYNCED: 'PROFILE_SYNCED',
  LOGOUT: 'LOGOUT',
  PROFILE_CLEARED: 'PROFILE_CLEARED',
} as const;

export type AppEventKeyType = (typeof AppEventKey)[keyof typeof AppEventKey];
