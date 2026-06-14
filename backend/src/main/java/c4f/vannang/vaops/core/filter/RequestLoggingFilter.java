package c4f.vannang.vaops.core.filter;

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
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long startTime = System.currentTimeMillis();
    String clientIp = getClientIp(request);
    String userAgent = request.getHeader("User-Agent");

    log.info("REQ_START method={} uri={} query={} clientIp={} userAgent={}",
        request.getMethod(),
        request.getRequestURI(),
        request.getQueryString(),
        clientIp,
        userAgent
    );

    try {
      filterChain.doFilter(request, response);
    } catch (Exception ex) {
      log.error("REQ_ERROR exceptionClass={} message={}",
          ex.getClass().getName(),
          ex.getMessage(),
          ex
      );
      throw ex;
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      log.info("REQ_END status={} duration={}ms contentLength={}",
          response.getStatus(),
          duration,
          response.getHeader("Content-Length")
      );
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
