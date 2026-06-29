package c4f.vannang.vaops.shared.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED"),

    UNAUTHORIZED(HttpStatus.FORBIDDEN, "ACCESS_DENIED"),

    VALIDATION(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED"),

    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND"),

    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "RESOURCE_ALREADY_EXISTS"),

    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT, "BUSINESS_RULE_VIOLATION"),

    INVALID_STATE(HttpStatus.CONFLICT, "INVALID_STATE"),

    LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "LIMIT_EXCEEDED"),

    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR"),

    EXTERNAL_SERVICE(HttpStatus.BAD_GATEWAY, "EXTERNAL_SERVICE_ERROR"),

    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "TIMEOUT"),

    DATA_INTEGRITY(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION"),

    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, "CONCURRENCY_CONFLICT"),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED"),

    ACCOUNT_LOCKED(HttpStatus.LOCKED, "ACCOUNT_LOCKED"),

    FILE_UPLOAD(HttpStatus.BAD_REQUEST, "FILE_UPLOAD_ERROR"),

    FILE_SIZE_LIMIT(HttpStatus.CONTENT_TOO_LARGE, "FILE_SIZE_LIMIT_EXCEEDED");

    private final HttpStatus status;
    private final String code;

    ErrorCode(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
