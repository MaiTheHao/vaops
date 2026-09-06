package c4f.vannang.vaops.core.web.filter;

import c4f.vannang.vaops.core.constant.AuthConstant;
import c4f.vannang.vaops.modules.identity.api.dto.CheckAvailableUserQuery;
import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import c4f.vannang.vaops.shared.feature.security.UserAuthenticationToken;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

  private final AccessTokenSpec accessTokenSpec;
  private final IdentityUserAPIService identityUserService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractTokenFromCookie(request);

    if (token != null) {
      AccessTokenClaims claims = accessTokenSpec.validate(token);
      identityUserService.checkAvailableUser(new CheckAvailableUserQuery(claims.userId()));

      AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
          claims.userId(), claims.accountName(), claims.roles(), claims.permissions());

      UserAuthenticationToken authentication =
          new UserAuthenticationToken(principal, buildAuthorities(claims));
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (AuthConstant.ACCESS_TOKEN_KEY.equals(cookie.getName())) {
        String token = cookie.getValue();
        if (StringUtils.hasText(token)) {
          return token;
        }
      }
    }

    return null;
  }

  private List<GrantedAuthority> buildAuthorities(AccessTokenClaims claims) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    if (claims.roles() != null) {
      claims.roles().stream()
          .filter(StringUtils::hasText)
          .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
          .map(SimpleGrantedAuthority::new)
          .forEach(authorities::add);
    }

    if (claims.permissions() != null) {
      claims.permissions().stream()
          .filter(StringUtils::hasText)
          .map(SimpleGrantedAuthority::new)
          .forEach(authorities::add);
    }

    return authorities;
  }
}
