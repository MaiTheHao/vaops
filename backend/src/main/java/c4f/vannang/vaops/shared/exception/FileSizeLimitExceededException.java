package c4f.vannang.vaops.shared.exception;

import java.util.Map;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;

public class FileSizeLimitExceededException extends AbstractPlatformException {

    public FileSizeLimitExceededException() {
        super(ErrorCode.FILE_SIZE_LIMIT, "Uploaded file size exceeds limit");
    }

    public FileSizeLimitExceededException(String message) {
        super(ErrorCode.FILE_SIZE_LIMIT, message);
    }

    public FileSizeLimitExceededException(String message, Throwable cause) {
        super(ErrorCode.FILE_SIZE_LIMIT, message, cause);
    }

    public FileSizeLimitExceededException(String message, Map<String, Object> details) {
        super(ErrorCode.FILE_SIZE_LIMIT, message, details);
    }
}
