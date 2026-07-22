package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class TimeoutException extends AbstractPlatformException {


    public TimeoutException() {
        super(ErrorCode.TIMEOUT, "Request timed out");
    }
    public TimeoutException(String message) {
        super(ErrorCode.TIMEOUT, message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(ErrorCode.TIMEOUT, message, cause);
    }

    public TimeoutException(String message, Map<String, Object> details) {
        super(ErrorCode.TIMEOUT, message, details);
    }
}
