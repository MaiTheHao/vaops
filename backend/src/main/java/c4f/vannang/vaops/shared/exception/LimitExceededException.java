package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class LimitExceededException extends AbstractPlatformException {


    public LimitExceededException() {
        super(ErrorCode.LIMIT_EXCEEDED, "Limit exceeded");
    }
    public LimitExceededException(String message) {
        super(ErrorCode.LIMIT_EXCEEDED, message);
    }

    public LimitExceededException(String message, Throwable cause) {
        super(ErrorCode.LIMIT_EXCEEDED, message, cause);
    }

    public LimitExceededException(String message, Map<String, Object> details) {
        super(ErrorCode.LIMIT_EXCEEDED, message, details);
    }
}
