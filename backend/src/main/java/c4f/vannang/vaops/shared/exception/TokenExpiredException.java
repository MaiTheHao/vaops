package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class TokenExpiredException extends AbstractPlatformException {

    public TokenExpiredException() {
        super(ErrorCode.TOKEN_EXPIRED, "Token has expired");
    }

    public TokenExpiredException(String message) {
        super(ErrorCode.TOKEN_EXPIRED, message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(ErrorCode.TOKEN_EXPIRED, message, cause);
    }

    public TokenExpiredException(String message, Map<String, Object> details) {
        super(ErrorCode.TOKEN_EXPIRED, message, details);
    }
}
