package c4f.vannang.vaops.shared.exception;

import java.util.Map;

public class FileException extends AbstractPlatformException {

    public FileException() {
        super(ErrorCode.FILE_UPLOAD, "File processing failed");
    }

    public FileException(String message) {
        super(ErrorCode.FILE_UPLOAD, message);
    }

    public FileException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public FileException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public FileException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }
}
