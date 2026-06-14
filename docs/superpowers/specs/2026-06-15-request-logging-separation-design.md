# Design Spec: Request Tracing and Logging Separation

## Goal
Separate the responsibilities of tracing (generating `requestId`, putting it into MDC/Request/Response Headers) and logging request lifecycle (START, END, ERROR with execution durations) into two distinct Servlet filters to keep the codebase clean, modular, and maintainable.

## Proposed Architecture

### Filter 1: `RequestTraceFilter`
- **Responsibility:** Only manages `requestId` generation, putting it into MDC, wrapping request headers, and setting response header. No lifecycle logging.
- **Cleanup:** Uses `MDC.remove("requestId")` in `finally` to prevent clearing other context keys.

### Filter 2: `RequestLoggingFilter`
- **Responsibility:** Logs request lifecycle events: START, END, and ERROR with durations.
- **Exclusion:** Does not log sensitive headers (like `Authorization`, `Cookie`, `Password`, `JWT`, `AccessToken`, `RefreshToken`).

### Registration and Ordering
Both filters are registered as Servlet Filters via `FilterConfig` using `FilterRegistrationBean` to ensure they execute before Spring Security and process all incoming requests:
- `RequestTraceFilter`: Order = `Ordered.HIGHEST_PRECEDENCE`
- `RequestLoggingFilter`: Order = `Ordered.HIGHEST_PRECEDENCE + 1`

## Proposed Code Changes

### `RequestTraceFilter.java`
Refactored to focus solely on tracing:
```java
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
```

### `RequestLoggingFilter.java`
Newly created logging filter:
```java
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
```

### `FilterConfig.java`
Updated to configure filter ordering:
```java
package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.core.filter.RequestLoggingFilter;
import c4f.vannang.vaops.core.filter.RequestTraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<RequestTraceFilter> requestTraceFilterRegistration() {
        FilterRegistrationBean<RequestTraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestTraceFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration() {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestLoggingFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
```
