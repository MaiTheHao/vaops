package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class InvalidStateException extends AbstractPlatformException {

    public InvalidStateException() {
        super(ErrorCode.INVALID_STATE, "Invalid entity or system state");
    }

    public InvalidStateException(String message) {
        super(ErrorCode.INVALID_STATE, message);
    }

    public InvalidStateException(String message, Throwable cause) {
        super(ErrorCode.INVALID_STATE, message, cause);
    }

    public InvalidStateException(String message, Map<String, Object> details) {
        super(ErrorCode.INVALID_STATE, message, details);
    }
}
