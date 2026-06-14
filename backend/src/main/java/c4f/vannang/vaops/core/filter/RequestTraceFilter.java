package c4f.vannang.vaops.core.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class RequestTraceFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (!StringUtils.hasText(requestId)) {
      requestId = UUID.randomUUID().toString();
    }

    response.setHeader(REQUEST_ID_HEADER, requestId);

    HttpServletRequest wrappedRequest = new TraceableRequestWrapper(request, requestId);

    try {
      MDC.put(MDC_KEY, requestId);
      filterChain.doFilter(wrappedRequest, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  private static class TraceableRequestWrapper extends HttpServletRequestWrapper {
    private final String requestId;

    public TraceableRequestWrapper(HttpServletRequest request, String requestId) {
      super(request);
      this.requestId = requestId;
    }

    @Override
    public String getHeader(String name) {
      if (REQUEST_ID_HEADER.equalsIgnoreCase(name)) {
        return requestId;
      }
      return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if (REQUEST_ID_HEADER.equalsIgnoreCase(name)) {
        return Collections.enumeration(Collections.singletonList(requestId));
      }
      return super.getHeaders(name);
    }
  }
}


