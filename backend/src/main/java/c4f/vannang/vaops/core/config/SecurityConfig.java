package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.core.env.CorsProperties;
import c4f.vannang.vaops.shared.filter.AuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuthenticationFilter authenticationFilter;
  private final CorsProperties corsProperties;

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf((abstractHttpConfig) -> abstractHttpConfig.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin((abstractHttpConfig) -> abstractHttpConfig.disable())
        .logout((abstractHttpConfig) -> abstractHttpConfig.disable())
        .httpBasic((abstractHttpConfig) -> abstractHttpConfig.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage())))
        .authorizeHttpRequests(auth -> auth.requestMatchers("/hello", "/api/v1/hello")
            .permitAll()
            .requestMatchers("/api/v1/auth/login")
            .permitAll()
            .requestMatchers("/api/v1/auth/register")
            .permitAll()
            .requestMatchers("/api/v1/auth/refresh")
            .permitAll()
            .requestMatchers("/api/v1/profile/**")
            .authenticated()
            .anyRequest()
            .authenticated())
        .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
    configuration.setAllowedMethods(corsProperties.getAllowedMethods());
    configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
    configuration.setAllowCredentials(corsProperties.getAllowCredentials());
    
    if (corsProperties.getExposedHeaders() != null && !corsProperties.getExposedHeaders().isEmpty()) {
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
    }
    if (corsProperties.getMaxAge() != null) {
        configuration.setMaxAge(corsProperties.getMaxAge());
    }

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
