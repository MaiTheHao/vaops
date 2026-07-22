package c4f.vannang.vaops.core.web.advice;

import c4f.vannang.vaops.shared.enumeration.ErrorCode;
import c4f.vannang.vaops.shared.exception.AbstractPlatformException;
import c4f.vannang.vaops.shared.exception.ErrorResponse;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "c4f.vannang.vaops")
public class PlatformExceptionHandler {

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

