package c4f.vannang.vaops.core.web.advice;

import c4f.vannang.vaops.shared.exception.AbstractPlatformException;
import c4f.vannang.vaops.shared.exception.ErrorCode;
import c4f.vannang.vaops.shared.exception.ErrorResponse;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

  private static final String MDC_KEY = "requestId";

  private String getRequestId() {
    String id = MDC.get(MDC_KEY);
    return id != null ? id : "N/A";
  }

  @ExceptionHandler(InternalServerException.class)
  public ResponseEntity<ErrorResponse> handleInternalServer(
      InternalServerException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.error("Internal server error [{}]: {}", reqId, ex.getMessage(), ex);
    if (ex.getCause() != null) {
      logCauseChain("Internal server error [" + reqId + "]", ex);
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ErrorCode.INTERNAL_SERVER.code(),
            ex.getMessage(),
            request.getRequestURI(),
            reqId,
            ex.getDetails()));
  }

  @ExceptionHandler(AbstractPlatformException.class)
  public ResponseEntity<ErrorResponse> handlePlatform(
      AbstractPlatformException ex, HttpServletRequest request) {
    ErrorCode ec = ex.getErrorCode();
    String reqId = getRequestId();

    if (ec.status().is5xxServerError()) {
      log.error("Platform system error [{}]: {}", reqId, ex.getMessage(), ex);
    } else {
      log.warn("Platform client error [{}]: {} - {}", reqId, ec.code(), ex.getMessage());
    }

    return ResponseEntity.status(ec.status())
        .body(ErrorResponse.of(
            ec.status().value(),
            ec.code(),
            ex.getMessage(),
            request.getRequestURI(),
            reqId,
            ex.getDetails()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    Map<String, Object> details = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      details.put(fe.getField(), fe.getDefaultMessage());
    }
    log.warn("Validation error [{}]: {}", reqId, details);
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Input validation failed",
            request.getRequestURI(),
            reqId,
            details));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    Map<String, Object> details = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(cv -> details.put(cv.getPropertyPath().toString(), cv.getMessage()));
    log.warn("Constraint violation [{}]: {}", reqId, details);
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_FAILED",
            "Constraint validation failed",
            request.getRequestURI(),
            reqId,
            details));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    Map<String, Object> details = new HashMap<>();
    details.put("field", ex.getName());
    details.put(
        "expected",
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
    details.put("actual", ex.getValue());
    log.warn(
        "Type mismatch error [{}]: field='{}', expected='{}', actual='{}'",
        reqId,
        ex.getName(),
        details.get("expected"),
        ex.getValue());
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "TYPE_MISMATCH",
            "Invalid value for field '" + ex.getName() + "'",
            request.getRequestURI(),
            reqId,
            details));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.error("Data integrity violation [{}]: {}", reqId, ex.getMessage(), ex);

    String message = "Database constraint violation";
    String code = "DATA_INTEGRITY_VIOLATION";

    Throwable rootCause = ex.getMostSpecificCause();
    if (rootCause instanceof java.sql.SQLException sqlEx) {
      String sqlState = sqlEx.getSQLState();
      if (sqlState != null) {
        switch (sqlState) {
          case "23505" -> {
            message = "Resource already exists";
            code = "RESOURCE_ALREADY_EXISTS";
          }
          case "23503" -> {
            message = "Referenced resource not found";
            code = "RESOURCE_NOT_FOUND";
          }
          case "23502" -> {
            message = "Required database field is missing";
            code = "VALIDATION_FAILED";
          }
        }
      }
    } else {
      String detailMessage = rootCause.getMessage();
      if (detailMessage != null) {
        if (detailMessage.contains("unique") || detailMessage.contains("duplicate")) {
          message = "Resource already exists";
          code = "RESOURCE_ALREADY_EXISTS";
        } else if (detailMessage.contains("foreign key")) {
          message = "Referenced resource not found";
          code = "RESOURCE_NOT_FOUND";
        }
      }
    }

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(
            HttpStatus.CONFLICT.value(), code, message, request.getRequestURI(), reqId, null));
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLock(
      ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("Optimistic locking failure [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            "CONCURRENCY_CONFLICT",
            "Concurrent update conflict, please retry",
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      AccessDeniedException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("Access denied [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "ACCESS_DENIED",
            "Insufficient permissions",
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("Malformed JSON request [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.badRequest()
        .body(ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MALFORMED_REQUEST",
            "Malformed JSON request payload",
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("HTTP method not supported [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(ErrorResponse.of(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "METHOD_NOT_ALLOWED",
            ex.getMessage(),
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("No handler found [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "RESOURCE_NOT_FOUND",
            "Endpoint not found",
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.error("Unhandled exception [{}]: ", reqId, ex);
    if (ex.getCause() != null) {
      logCauseChain("Unhandled exception [" + reqId + "]", ex);
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            reqId,
            null));
  }

  private static void logCauseChain(String prefix, Throwable throwable) {
    if (throwable == null) return;

    Throwable cause = throwable.getCause();
    int depth = 0;
    while (cause != null && depth < 10) {
      log.error(
          "{} Caused by [{}]: {}: {}",
          prefix,
          depth + 1,
          cause.getClass().getName(),
          cause.getMessage());
      cause = cause.getCause();
      depth++;
    }
  }
}
