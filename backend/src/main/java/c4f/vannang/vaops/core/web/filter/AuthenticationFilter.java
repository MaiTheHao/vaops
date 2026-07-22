package c4f.vannang.vaops.core.web.filter;

import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import c4f.vannang.vaops.shared.security.UserAuthenticationToken;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";

  @Qualifier("handlerExceptionResolver")
  private final HandlerExceptionResolver handlerExceptionResolver;

  private final AccessTokenSpec accessTokenSpec;
  private final IdentityModuleApi identityModuleApi;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null) {
      try {
        AccessTokenClaims claims = accessTokenSpec.validateAccessToken(token);

        identityModuleApi.checkAvailableUser(claims.userId());

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            claims.userId(), claims.accountName());

        UserAuthenticationToken authentication =
            new UserAuthenticationToken(principal, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (Exception e) {
        SecurityContextHolder.clearContext();
        this.handlerExceptionResolver.resolveException(request, response, null, e);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }
}
