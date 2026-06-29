package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class ExternalServiceException extends AbstractPlatformException {


    public ExternalServiceException() {
        super(ErrorCode.EXTERNAL_SERVICE, "External service error");
    }
    public ExternalServiceException(String message) {
        super(ErrorCode.EXTERNAL_SERVICE, message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(ErrorCode.EXTERNAL_SERVICE, message, cause);
    }

    public ExternalServiceException(String message, Map<String, Object> details) {
        super(ErrorCode.EXTERNAL_SERVICE, message, details);
    }
}
