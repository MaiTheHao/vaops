package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class UnauthenticatedException extends AbstractPlatformException {

    public UnauthenticatedException() {
        super(ErrorCode.UNAUTHENTICATED, "Authentication failed");
    }

    public UnauthenticatedException(String message) {
        super(ErrorCode.UNAUTHENTICATED, message);
    }

    public UnauthenticatedException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHENTICATED, message, cause);
    }

    public UnauthenticatedException(String message, Map<String, Object> details) {
        super(ErrorCode.UNAUTHENTICATED, message, details);
    }
}
