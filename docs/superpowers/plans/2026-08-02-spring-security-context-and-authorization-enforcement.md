# Spring Security Context & Method-Level Authorization Enforcement Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Update `AuthenticatedPrincipal` and `AuthenticationFilter` to map roles into internal Spring Security authorities with `ROLE_` prefix (enabling `hasRole('...')`) while keeping raw strings in claims and `AuthenticatedPrincipal.roles()`, enable `@EnableMethodSecurity`, and enforce `@PreAuthorize` authorization checks on all REST API endpoints.

**Architecture:** `AuthenticationFilter` extracts `roles` and `permissions` from JWT `AccessTokenClaims`, maps `roles` to `SimpleGrantedAuthority("ROLE_" + roleCode)` internally and `permissions` to `SimpleGrantedAuthority(permissionString)`. `@EnableMethodSecurity` enables method-level `@PreAuthorize("hasAuthority('...')")` and `@PreAuthorize("hasRole('...')")` checks across all controllers.

**Tech Stack:** Java 21, Spring Boot 3, Spring Security, JUnit 5, Mockito, MockMvc.

## Global Constraints

- External Authority Storage: Raw strings without prefixing in claims & `AuthenticatedPrincipal` (e.g. `'SUPER_ADMIN'`, `'ADMIN'`, `'USER:READ'`, `'ROLE:CREATE'`).
- Internal Spring Security Context: Roles get `ROLE_` prefix automatically inside `AuthenticationFilter` (e.g. `GrantedAuthority` contains `'ROLE_ADMIN'`, enabling `hasRole('ADMIN')`).
- Method Security: `@EnableMethodSecurity(prePostEnabled = true)` on `SecurityConfig`.

---

## File Structure Map

```
backend/src/main/java/c4f/vannang/vaops/
├── core/
│   ├── config/
│   │   └── SecurityConfig.java (modified)
│   └── web/filter/
│       └── AuthenticationFilter.java (modified)
├── shared/feature/security/
│   └── AuthenticatedPrincipal.java (modified)
├── modules/authorization/infrastructure/web/controller/
│   ├── RoleController.java (modified)
│   ├── PermissionController.java (modified)
│   └── UserRoleController.java (modified)
├── modules/identity/infrastructure/web/controller/
│   └── ProfileController.java (modified)
└── modules/authentication/infrastructure/web/controller/
    └── AuthenticationController.java (modified)

docs/srs.md (modified)
```

---

### Task 1: Update `AuthenticatedPrincipal` and `AuthenticationFilter`

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/shared/feature/security/AuthenticatedPrincipal.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilter.java`
- Create Test: `backend/src/test/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilterTest.java`

- [ ] **Step 1: Write failing test for `AuthenticationFilter`**

Create `backend/src/test/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilterTest.java`:
```java
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
```

- [ ] **Step 2: Run test to verify failure**

Run: `./mvnw test -pl backend -Dtest=AuthenticationFilterTest`
Expected: Failure because `AuthenticatedPrincipal` does not store roles/permissions yet and `AuthenticationFilter` passes `Collections.emptyList()`.

- [ ] **Step 3: Update `AuthenticatedPrincipal.java`**

Modify `backend/src/main/java/c4f/vannang/vaops/shared/feature/security/AuthenticatedPrincipal.java`:
```java
package c4f.vannang.vaops.shared.feature.security;

import java.util.List;
import java.util.UUID;

public record AuthenticatedPrincipal(
    UUID userId,
    String accountName,
    List<String> roles,
    List<String> permissions
) {
  public AuthenticatedPrincipal {
    roles = roles == null ? List.of() : roles;
    permissions = permissions == null ? List.of() : permissions;
  }

  public AuthenticatedPrincipal(UUID userId, String accountName) {
    this(userId, accountName, List.of(), List.of());
  }
}
```

- [ ] **Step 4: Update `AuthenticationFilter.java`**

Modify `backend/src/main/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilter.java`:
```java
package c4f.vannang.vaops.core.web.filter;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
  private final IdentityUserAPIService identityUserService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null) {
      try {
        AccessTokenClaims claims = accessTokenSpec.validate(token);

        identityUserService.checkAvailableUser(new CheckAvailableUserQuery(claims.userId()));

        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            claims.userId(), claims.accountName(), claims.roles(), claims.permissions());

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (claims.roles() != null) {
          claims.roles().stream()
              .filter(StringUtils::hasText)
              .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
              .map(SimpleGrantedAuthority::new)
              .forEach(authorities::add);
        }
        if (claims.permissions() != null) {
          claims.permissions().stream()
              .filter(StringUtils::hasText)
              .map(SimpleGrantedAuthority::new)
              .forEach(authorities::add);
        }

        UserAuthenticationToken authentication =
            new UserAuthenticationToken(principal, authorities);
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
```

- [ ] **Step 5: Run test to verify it passes**

Run: `./mvnw test -pl backend -Dtest=AuthenticationFilterTest`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/feature/security/AuthenticatedPrincipal.java
git add backend/src/main/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilter.java
git add backend/src/test/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilterTest.java
git commit -m "feat(security): populate raw role and permission authorities with ROLE_ prefix for Spring Security Context"
```

---

### Task 2: Enable `@EnableMethodSecurity` and add `@PreAuthorize` to Controllers

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/RoleController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/PermissionController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/UserRoleController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/identity/infrastructure/web/controller/ProfileController.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java`

- [ ] **Step 1: Enable `@EnableMethodSecurity` in `SecurityConfig.java`**

Modify `SecurityConfig.java`: Add `@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity` to class annotations.

- [ ] **Step 2: Add `@PreAuthorize` to `RoleController.java`**

Add annotations:
- `createRole`: `@PreAuthorize("hasAuthority('ROLE:CREATE') or hasRole('SUPER_ADMIN')")`
- `updateRole`: `@PreAuthorize("hasAuthority('ROLE:UPDATE') or hasRole('SUPER_ADMIN')")`
- `deleteRole`: `@PreAuthorize("hasAuthority('ROLE:DELETE') or hasRole('SUPER_ADMIN')")`
- `getRoleById`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `searchRoles`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `assignPermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")`
- `revokePermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")`

- [ ] **Step 3: Add `@PreAuthorize` to `PermissionController.java`**

Add annotations:
- `createPermission`: `@PreAuthorize("hasAuthority('PERMISSION:CREATE') or hasRole('SUPER_ADMIN')")`
- `updatePermission`: `@PreAuthorize("hasAuthority('PERMISSION:UPDATE') or hasRole('SUPER_ADMIN')")`
- `deletePermission`: `@PreAuthorize("hasAuthority('PERMISSION:DELETE') or hasRole('SUPER_ADMIN')")`
- `getPermissionById`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`
- `searchPermissions`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`

- [ ] **Step 4: Add `@PreAuthorize` to `UserRoleController.java`**

Add annotations:
- `assignRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")`
- `revokeRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")`

- [ ] **Step 5: Add `@PreAuthorize` to `ProfileController.java`**

Add annotations:
- `getProfile`: `@PreAuthorize("hasAuthority('PROFILE:READ')")`
- `updateProfile`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`
- `changePassword`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`

- [ ] **Step 6: Add `@PreAuthorize` to `AuthenticationController.java`**

Add annotation:
- `logout`: `@PreAuthorize("isAuthenticated()")`

- [ ] **Step 7: Verify compilation & run tests**

Run: `./mvnw test -pl backend`
Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java
git add backend/src/main/java/c4f/vannang/vaops/modules/
git commit -m "feat(security): enable method security and add PreAuthorize with hasRole and hasAuthority to API controllers"
```

---

### Task 3: Documentation Walkthrough Update in `docs/srs.md`

**Files:**
- Modify: `docs/srs.md`

- [ ] **Step 1: Update `docs/srs.md` Walkthrough Log**

Update Section 9 and 10 in `docs/srs.md` to log completion of Spring Security Context integration and `@PreAuthorize` method security enforcement across all endpoints.

- [ ] **Step 2: Commit**

```bash
git add docs/srs.md
git commit -m "docs(srs): update walkthrough log for Spring Security Context and method authorization"
```
