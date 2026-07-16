package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.modules.authentication.infrastructure.web.filter.AuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationFilter authenticationFilter;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf((abstractHttpConfig) -> abstractHttpConfig.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin((abstractHttpConfig) -> abstractHttpConfig.disable())
        .logout((abstractHttpConfig) -> abstractHttpConfig.disable())
        .httpBasic((abstractHttpConfig) -> abstractHttpConfig.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage())))
        .authorizeHttpRequests(auth -> auth.requestMatchers("/hello", "/v1/hello")
            .permitAll()
            .requestMatchers("/api/v1/auth/login")
            .permitAll()
            .requestMatchers("/api/v1/auth/register")
            .permitAll()
            .requestMatchers("/api/v1/auth/refresh")
            .permitAll()
            .anyRequest()
            .authenticated())
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
