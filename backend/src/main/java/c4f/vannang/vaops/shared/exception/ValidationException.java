package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class ValidationException extends AbstractPlatformException {


    public ValidationException() {
        super(ErrorCode.VALIDATION, "Validation failed");
    }
    public ValidationException(String message) {
        super(ErrorCode.VALIDATION, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION, message, cause);
    }

    public ValidationException(String message, Map<String, Object> details) {
        super(ErrorCode.VALIDATION, message, details);
    }
}
