package c4f.vannang.vaops.core.web.advice;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;
import c4f.vannang.vaops.shared.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String MDC_KEY = "requestId";

  private String getRequestId() {
    String id = MDC.get(MDC_KEY);
    return id != null ? id : "N/A";
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
            ErrorCode.VALIDATION.code(),
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
            ErrorCode.VALIDATION.code(),
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
            ErrorCode.TYPE_MISMATCH.code(),
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
    String code = ErrorCode.DATA_INTEGRITY.code();

    Throwable rootCause = ex.getMostSpecificCause();
    if (rootCause instanceof java.sql.SQLException sqlEx) {
      String sqlState = sqlEx.getSQLState();
      if (sqlState != null) {
        switch (sqlState) {
          case "23505" -> {
            message = "Resource already exists";
            code = ErrorCode.RESOURCE_ALREADY_EXISTS.code();
          }
          case "23503" -> {
            message = "Referenced resource not found";
            code = ErrorCode.RESOURCE_NOT_FOUND.code();
          }
          case "23502" -> {
            message = "Required database field is missing";
            code = ErrorCode.VALIDATION.code();
          }
        }
      }
    } else {
      String detailMessage = rootCause.getMessage();
      if (detailMessage != null) {
        if (detailMessage.contains("unique") || detailMessage.contains("duplicate")) {
          message = "Resource already exists";
          code = ErrorCode.RESOURCE_ALREADY_EXISTS.code();
        } else if (detailMessage.contains("foreign key")) {
          message = "Referenced resource not found";
          code = ErrorCode.RESOURCE_NOT_FOUND.code();
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
            ErrorCode.CONCURRENCY_CONFLICT.code(),
            "Concurrent update conflict, please retry",
            request.getRequestURI(),
            reqId,
            null));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthentication(
      AuthenticationException ex, HttpServletRequest request) {
    String reqId = getRequestId();
    log.warn("Authentication failed [{}]: {}", reqId, ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ErrorCode.UNAUTHENTICATED.code(),
            "Authentication failed. Please sign in again.",
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
            ErrorCode.UNAUTHORIZED.code(),
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
            ErrorCode.MALFORMED_REQUEST.code(),
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
            ErrorCode.METHOD_NOT_ALLOWED.code(),
            "Method not allowed",
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
            ErrorCode.RESOURCE_NOT_FOUND.code(),
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
            ErrorCode.INTERNAL_SERVER.code(),
            "An unexpected error occurred. Please try again later.",
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
