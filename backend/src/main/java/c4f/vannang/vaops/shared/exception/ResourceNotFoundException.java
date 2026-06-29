package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class ResourceNotFoundException extends AbstractPlatformException {


    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND, "Resource not found");
    }
    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, cause);
    }

    public ResourceNotFoundException(String message, Map<String, Object> details) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, details);
    }
}
