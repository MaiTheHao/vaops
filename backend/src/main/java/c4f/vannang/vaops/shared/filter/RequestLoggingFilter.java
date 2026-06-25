package c4f.vannang.vaops.shared.filter;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final long startTime = System.nanoTime();

    final String method = request.getMethod();
    final String uri = sanitize(request.getRequestURI());
    final String query = sanitize(request.getQueryString());
    final String clientIp = getClientIp(request);
    final String userAgent = sanitize(request.getHeader("User-Agent"));

    log.info("REQ_START method={} uri={} query={} clientIp={} userAgent={}", method, uri, query, clientIp, userAgent);

    try {
      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      log.error("REQ_ERROR method={} uri={} exceptionClass={} message={}", method, uri, ex.getClass().getName(),
          sanitize(ex.getMessage()), ex);
      throw ex;
    } finally {
      final long durationMs = (System.nanoTime() - startTime) / 1_000_000;

      log.info("REQ_END method={} uri={} status={} duration={}ms contentLength={}", method, uri, response.getStatus(),
          durationMs, response.getHeader("Content-Length"));
    }
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
