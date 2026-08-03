export const TranslateKey = {
  auth: {
    label: {
      accountName: 'auth.label.accountName',
      displayName: 'auth.label.displayName',
      password: 'auth.label.password',
      confirmPassword: 'auth.label.confirmPassword',
      avatarUrl: 'auth.label.avatarUrl',
    },
    placeholder: {
      accountName: 'auth.placeholder.accountName',
      displayName: 'auth.placeholder.displayName',
    },
    btn: {
      loginSubmit: 'auth.btn.loginSubmit',
      registerSubmit: 'auth.btn.registerSubmit',
    },
    dialog: {
      registerError: 'auth.dialog.registerError',
      passwordMismatch: 'auth.dialog.passwordMismatch',
    },
  },
} as const;

type Leaves<T> = T extends string
  ? T
  : { [K in keyof T]: Leaves<T[K]> }[keyof T];

export type TranslateKeyType = Leaves<typeof TranslateKey>;