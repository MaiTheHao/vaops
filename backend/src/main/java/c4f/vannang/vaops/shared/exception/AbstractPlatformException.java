package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public abstract class AbstractPlatformException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Map<String, Object> details;

    protected AbstractPlatformException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    protected AbstractPlatformException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, null);
    }

    protected AbstractPlatformException(ErrorCode errorCode, String message, Map<String, Object> details) {
        this(errorCode, message, null, details);
    }

    protected AbstractPlatformException(ErrorCode errorCode, String message, Throwable cause,
            Map<String, Object> details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
