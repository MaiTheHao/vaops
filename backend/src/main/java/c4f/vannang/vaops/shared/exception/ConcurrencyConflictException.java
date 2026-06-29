package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class ConcurrencyConflictException extends AbstractPlatformException {


    public ConcurrencyConflictException() {
        super(ErrorCode.CONCURRENCY_CONFLICT, "Concurrency conflict occurred");
    }
    public ConcurrencyConflictException(String message) {
        super(ErrorCode.CONCURRENCY_CONFLICT, message);
    }

    public ConcurrencyConflictException(String message, Throwable cause) {
        super(ErrorCode.CONCURRENCY_CONFLICT, message, cause);
    }

    public ConcurrencyConflictException(String message, Map<String, Object> details) {
        super(ErrorCode.CONCURRENCY_CONFLICT, message, details);
    }
}
