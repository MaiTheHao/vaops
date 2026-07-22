package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class InternalServerException extends AbstractPlatformException {


    public InternalServerException() {
        super(ErrorCode.INTERNAL_SERVER, "Internal server error");
    }
    public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_SERVER, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(ErrorCode.INTERNAL_SERVER, message, cause);
    }

    public InternalServerException(String message, Map<String, Object> details) {
        super(ErrorCode.INTERNAL_SERVER, message, details);
    }
}
