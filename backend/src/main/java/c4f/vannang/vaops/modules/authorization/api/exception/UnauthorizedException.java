package c4f.vannang.vaops.modules.authorization.api.exception;

import java.util.Map;
import c4f.vannang.vaops.shared.exception.AbstractPlatformException;
import c4f.vannang.vaops.shared.exception.ErrorCode;

public class UnauthorizedException extends AbstractPlatformException {

    public UnauthorizedException() {
        super(ErrorCode.UNAUTHORIZED, "Access denied");
    }

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, message, cause);
    }

    public UnauthorizedException(String message, Map<String, Object> details) {
        super(ErrorCode.UNAUTHORIZED, message, details);
    }
}
