package c4f.vannang.vaops.core.web.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import c4f.vannang.vaops.modules.identity.api.service.IdentityUserAPIService;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import c4f.vannang.vaops.shared.feature.security.UserAuthenticationToken;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

  @Mock private HandlerExceptionResolver handlerExceptionResolver;
  @Mock private AccessTokenSpec accessTokenSpec;
  @Mock private IdentityUserAPIService identityUserService;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain filterChain;

  private AuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    filter = new AuthenticationFilter(handlerExceptionResolver, accessTokenSpec, identityUserService);
  }

  @Test
  void doFilterInternal_shouldPopulateAuthorities_withInternalRolePrefix() throws Exception {
    UUID userId = UUID.randomUUID();
    List<String> roles = List.of("ADMIN");
    List<String> permissions = List.of("USER:READ", "ROLE:CREATE");
    AccessTokenClaims claims = new AccessTokenClaims(userId, "admin.user", roles, permissions);

    when(request.getHeader("Authorization")).thenReturn("Bearer sample-valid-token");
    when(accessTokenSpec.validate("sample-valid-token")).thenReturn(claims);

    filter.doFilterInternal(request, response, filterChain);

    UserAuthenticationToken auth = (UserAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(auth);

    AuthenticatedPrincipal principal = auth.getPrincipal();
    assertEquals(userId, principal.userId());
    assertEquals("admin.user", principal.accountName());
    assertEquals(roles, principal.roles()); // Raw "ADMIN" in principal
    assertEquals(permissions, principal.permissions());

    List<String> authorityStrings = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    // Internal Spring Security GrantedAuthorities have ROLE_ prefix for roles
    assertTrue(authorityStrings.contains("ROLE_ADMIN"));
    assertTrue(authorityStrings.contains("USER:READ"));
    assertTrue(authorityStrings.contains("ROLE:CREATE"));

    verify(filterChain).doFilter(request, response);
  }
}
