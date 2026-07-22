package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class TypeMismatchException extends AbstractPlatformException {


    public TypeMismatchException() {
        super(ErrorCode.TYPE_MISMATCH, "Type mismatch occurred");
    }
    public TypeMismatchException(String message) {
        super(ErrorCode.TYPE_MISMATCH, message);
    }

    public TypeMismatchException(String message, Throwable cause) {
        super(ErrorCode.TYPE_MISMATCH, message, cause);
    }

    public TypeMismatchException(String message, Map<String, Object> details) {
        super(ErrorCode.TYPE_MISMATCH, message, details);
    }
}
