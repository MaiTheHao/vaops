package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class ResourceAlreadyExistsException extends AbstractPlatformException {


    public ResourceAlreadyExistsException() {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, "Resource already exists");
    }
    public ResourceAlreadyExistsException(String message) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
    }

    public ResourceAlreadyExistsException(String message, Throwable cause) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, cause);
    }

    public ResourceAlreadyExistsException(String message, Map<String, Object> details) {
        super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, details);
    }
}
