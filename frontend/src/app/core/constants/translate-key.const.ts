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
      loginError: 'auth.dialog.loginError',
      registerError: 'auth.dialog.registerError',
      passwordMismatch: 'auth.dialog.passwordMismatch',
      loginFailedMessage: 'auth.dialog.loginFailedMessage',
      registerFailedMessage: 'auth.dialog.registerFailedMessage',
      loginSuccess: 'auth.dialog.loginSuccess',
      registerSuccess: 'auth.dialog.registerSuccess',
    },
  },
  error: {
    domain: {
      VALIDATION_FAILED: {
        title: 'error.domain.VALIDATION_FAILED.title',
        message: 'error.domain.VALIDATION_FAILED.message',
      },
      TYPE_MISMATCH: {
        title: 'error.domain.TYPE_MISMATCH.title',
        message: 'error.domain.TYPE_MISMATCH.message',
      },
      MALFORMED_REQUEST: {
        title: 'error.domain.MALFORMED_REQUEST.title',
        message: 'error.domain.MALFORMED_REQUEST.message',
      },
      FILE_UPLOAD_ERROR: {
        title: 'error.domain.FILE_UPLOAD_ERROR.title',
        message: 'error.domain.FILE_UPLOAD_ERROR.message',
      },
      AUTHENTICATION_FAILED: {
        title: 'error.domain.AUTHENTICATION_FAILED.title',
        message: 'error.domain.AUTHENTICATION_FAILED.message',
      },
      TOKEN_EXPIRED: {
        title: 'error.domain.TOKEN_EXPIRED.title',
        message: 'error.domain.TOKEN_EXPIRED.message',
      },
      ACCESS_DENIED: {
        title: 'error.domain.ACCESS_DENIED.title',
        message: 'error.domain.ACCESS_DENIED.message',
      },
      RESOURCE_NOT_FOUND: {
        title: 'error.domain.RESOURCE_NOT_FOUND.title',
        message: 'error.domain.RESOURCE_NOT_FOUND.message',
      },
      METHOD_NOT_ALLOWED: {
        title: 'error.domain.METHOD_NOT_ALLOWED.title',
        message: 'error.domain.METHOD_NOT_ALLOWED.message',
      },
      RESOURCE_ALREADY_EXISTS: {
        title: 'error.domain.RESOURCE_ALREADY_EXISTS.title',
        message: 'error.domain.RESOURCE_ALREADY_EXISTS.message',
      },
      DATA_INTEGRITY_VIOLATION: {
        title: 'error.domain.DATA_INTEGRITY_VIOLATION.title',
        message: 'error.domain.DATA_INTEGRITY_VIOLATION.message',
      },
      CONCURRENCY_CONFLICT: {
        title: 'error.domain.CONCURRENCY_CONFLICT.title',
        message: 'error.domain.CONCURRENCY_CONFLICT.message',
      },
      INVALID_STATE: {
        title: 'error.domain.INVALID_STATE.title',
        message: 'error.domain.INVALID_STATE.message',
      },
      FILE_SIZE_LIMIT_EXCEEDED: {
        title: 'error.domain.FILE_SIZE_LIMIT_EXCEEDED.title',
        message: 'error.domain.FILE_SIZE_LIMIT_EXCEEDED.message',
      },
      BUSINESS_RULE_VIOLATION: {
        title: 'error.domain.BUSINESS_RULE_VIOLATION.title',
        message: 'error.domain.BUSINESS_RULE_VIOLATION.message',
      },
      ACCOUNT_LOCKED: {
        title: 'error.domain.ACCOUNT_LOCKED.title',
        message: 'error.domain.ACCOUNT_LOCKED.message',
      },
      LIMIT_EXCEEDED: {
        title: 'error.domain.LIMIT_EXCEEDED.title',
        message: 'error.domain.LIMIT_EXCEEDED.message',
      },
      INTERNAL_ERROR: {
        title: 'error.domain.INTERNAL_ERROR.title',
        message: 'error.domain.INTERNAL_ERROR.message',
      },
      EXTERNAL_SERVICE_ERROR: {
        title: 'error.domain.EXTERNAL_SERVICE_ERROR.title',
        message: 'error.domain.EXTERNAL_SERVICE_ERROR.message',
      },
      TIMEOUT: {
        title: 'error.domain.TIMEOUT.title',
        message: 'error.domain.TIMEOUT.message',
      },
      UNKNOWN_ERROR: {
        title: 'error.domain.UNKNOWN_ERROR.title',
        message: 'error.domain.UNKNOWN_ERROR.message',
      },
    },
  },
} as const;

type Leaves<T> = T extends string
  ? T
  : { [K in keyof T]: Leaves<T[K]> }[keyof T];

export type TranslateKeyType = Leaves<typeof TranslateKey>;