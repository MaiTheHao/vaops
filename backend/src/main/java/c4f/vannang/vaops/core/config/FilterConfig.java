package c4f.vannang.vaops.core.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import c4f.vannang.vaops.shared.filter.RequestLoggingFilter;
import c4f.vannang.vaops.shared.filter.RequestTraceFilter;

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
