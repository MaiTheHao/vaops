package c4f.vannang.vaops.core.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final long startTime = System.nanoTime();

    final String requestId = request.getHeader(RequestTraceFilter.REQUEST_ID_HEADER);
    final String method = request.getMethod();
    final String uri = sanitize(request.getRequestURI());
    final String query = sanitize(request.getQueryString());
    final String clientIp = getClientIp(request);
    final String userAgent = sanitize(request.getHeader("User-Agent"));

    log.info(
        "REQ_START requestId={} method={} uri={} query={} clientIp={} userAgent={}",
        requestId,
        method,
        uri,
        query,
        clientIp,
        userAgent);

    if (log.isDebugEnabled()) {
      log.debug("REQ_HEADERS requestId={} headers=[{}]", requestId, getRequestHeaders(request));
    }

    try {
      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      log.error(
          "REQ_ERROR requestId={} method={} uri={} exceptionClass={} message={}",
          requestId,
          method,
          uri,
          ex.getClass().getName(),
          sanitize(ex.getMessage()),
          ex);
      throw ex;
    } finally {
      final long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      log.info(
          "REQ_END requestId={} method={} uri={} status={} duration={}ms contentLength={}",
          requestId,
          method,
          uri,
          response.getStatus(),
          durationMs,
          response.getHeader("Content-Length"));

      if (log.isTraceEnabled()) {
        log.trace("RES_HEADERS requestId={} headers=[{}]", requestId, getResponseHeaders(response));
      }
    }
  }

  private String getRequestHeaders(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    java.util.Enumeration<String> headerNames = request.getHeaderNames();
    if (headerNames != null) {
      while (headerNames.hasMoreElements()) {
        String name = headerNames.nextElement();
        // Mask sensitive header values like Authorization
        String value = "Authorization".equalsIgnoreCase(name) ? "******" : request.getHeader(name);
        sb.append(name).append(": ").append(sanitize(value)).append("; ");
      }
    }
    return sb.toString();
  }

  private String getResponseHeaders(HttpServletResponse response) {
    StringBuilder sb = new StringBuilder();
    java.util.Collection<String> headerNames = response.getHeaderNames();
    if (headerNames != null) {
      for (String name : headerNames) {
        String value = response.getHeader(name);
        sb.append(name).append(": ").append(sanitize(value)).append("; ");
      }
    }
    return sb.toString();
  }

  private String getClientIp(HttpServletRequest request) {
    final String xForwardedFor = request.getHeader("X-Forwarded-For");

    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      final int commaIndex = xForwardedFor.indexOf(',');
      return (commaIndex > 0 ? xForwardedFor.substring(0, commaIndex) : xForwardedFor).trim();
    }

    return request.getRemoteAddr();
  }

  private String sanitize(String value) {
    return value == null ? null : value.replace('\n', '_').replace('\r', '_');
  }
}
