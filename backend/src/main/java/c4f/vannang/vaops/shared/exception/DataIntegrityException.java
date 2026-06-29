package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class DataIntegrityException extends AbstractPlatformException {


    public DataIntegrityException() {
        super(ErrorCode.DATA_INTEGRITY, "Data integrity violation occurred");
    }
    public DataIntegrityException(String message) {
        super(ErrorCode.DATA_INTEGRITY, message);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(ErrorCode.DATA_INTEGRITY, message, cause);
    }

    public DataIntegrityException(String message, Map<String, Object> details) {
        super(ErrorCode.DATA_INTEGRITY, message, details);
    }
}
