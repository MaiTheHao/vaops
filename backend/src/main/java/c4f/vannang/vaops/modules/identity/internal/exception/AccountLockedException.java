package c4f.vannang.vaops.modules.identity.internal.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;
import c4f.vannang.vaops.shared.exception.AbstractPlatformException;

public class AccountLockedException extends AbstractPlatformException {

    public AccountLockedException() {
        super(ErrorCode.ACCOUNT_LOCKED, "Account is locked");
    }

    public AccountLockedException(String message) {
        super(ErrorCode.ACCOUNT_LOCKED, message);
    }

    public AccountLockedException(String message, Throwable cause) {
        super(ErrorCode.ACCOUNT_LOCKED, message, cause);
    }

    public AccountLockedException(String message, Map<String, Object> details) {
        super(ErrorCode.ACCOUNT_LOCKED, message, details);
    }
}
