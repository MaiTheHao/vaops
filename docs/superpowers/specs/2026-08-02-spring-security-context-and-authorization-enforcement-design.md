# Design Spec: Spring Security Context & Method-Level Authorization Enforcement

- **Date**: 2026-08-02
- **Topic**: Integration of roles and permissions into `AuthenticatedPrincipal` and `UserAuthenticationToken`, internal `ROLE_` prefixing for Spring Security Context authorities, enabling `@EnableMethodSecurity`, and applying `@PreAuthorize` authorization controls across all system API endpoints.
- **SRS Reference**: [docs/srs.md](file:///home/maithehao/Workspace/projects/vaops/docs/srs.md) Section 5 & NFR-SEC-1

---

## 1. Overview & Purpose

This design completes the Spring Security integration for the `vaops` backend by:
1. Enhancing `AuthenticatedPrincipal` to store raw `roles` (e.g. `"ADMIN"`) and `permissions` (e.g. `"USER:READ"`) extracted from JWT Access Tokens.
2. Updating `AuthenticationFilter` to populate Spring Security `GrantedAuthority` objects into `SecurityContextHolder`:
   - For **Roles**: Automatically prepends `ROLE_` prefix if not already present (e.g. `"ADMIN"` $\rightarrow$ `"ROLE_ADMIN"`) ONLY within Spring Security Context. This enables Spring Security's native `hasRole('ADMIN')` expression.
   - For **Permissions**: Kept as raw `RESOURCE:ACTION` strings (e.g. `"USER:READ"`). Checked via `hasAuthority('USER:READ')`.
3. Enabling `@EnableMethodSecurity` in `SecurityConfig`.
4. Adding `@PreAuthorize` method security annotations across all existing controllers (`RoleController`, `PermissionController`, `UserRoleController`, `ProfileController`, `AuthenticationController`).

---

## 2. Component Specifications

### 2.1 Security Core Models (`shared.feature.security`)

#### 1. `AuthenticatedPrincipal`
Stores clean, raw strings outside Spring Security Context:
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

#### 2. `AuthenticationFilter` (`core.web.filter`)
In `doFilterInternal(...)`:
```java
AccessTokenClaims claims = accessTokenSpec.validate(token);
identityUserService.checkAvailableUser(new CheckAvailableUserQuery(claims.userId()));

AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
    claims.userId(),
    claims.accountName(),
    claims.roles(),
    claims.permissions()
);

List<GrantedAuthority> authorities = new ArrayList<>();

// Map roles: attach ROLE_ prefix for Spring Security internal context
if (claims.roles() != null) {
  claims.roles().stream()
      .filter(StringUtils::hasText)
      .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
      .map(SimpleGrantedAuthority::new)
      .forEach(authorities::add);
}

// Map permissions: raw RESOURCE:ACTION strings
if (claims.permissions() != null) {
  claims.permissions().stream()
      .filter(StringUtils::hasText)
      .map(SimpleGrantedAuthority::new)
      .forEach(authorities::add);
}

UserAuthenticationToken authentication = new UserAuthenticationToken(principal, authorities);
authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
SecurityContextHolder.getContext().setAuthentication(authentication);
```

#### 3. `SecurityConfig` (`core.config`)
Annotate class with `@EnableMethodSecurity`:
```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  // ... SecurityFilterChain bean
}
```

---

### 2.2 Controller Method Security Annotations (`@PreAuthorize`)

#### 1. `RoleController` (`/api/v1/roles`)
- `createRole`: `@PreAuthorize("hasAuthority('ROLE:CREATE') or hasRole('SUPER_ADMIN')")`
- `updateRole`: `@PreAuthorize("hasAuthority('ROLE:UPDATE') or hasRole('SUPER_ADMIN')")`
- `deleteRole`: `@PreAuthorize("hasAuthority('ROLE:DELETE') or hasRole('SUPER_ADMIN')")`
- `getRoleById`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `searchRoles`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `assignPermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")`
- `revokePermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION') or hasRole('SUPER_ADMIN')")`

#### 2. `PermissionController` (`/api/v1/permissions`)
- `createPermission`: `@PreAuthorize("hasAuthority('PERMISSION:CREATE') or hasRole('SUPER_ADMIN')")`
- `updatePermission`: `@PreAuthorize("hasAuthority('PERMISSION:UPDATE') or hasRole('SUPER_ADMIN')")`
- `deletePermission`: `@PreAuthorize("hasAuthority('PERMISSION:DELETE') or hasRole('SUPER_ADMIN')")`
- `getPermissionById`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`
- `searchPermissions`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`

#### 3. `UserRoleController` (`/api/v1/users/{userId}/roles`)
- `assignRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")`
- `revokeRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE') or hasRole('SUPER_ADMIN')")`

#### 4. `ProfileController` (`/api/v1/profile`)
- `getProfile`: `@PreAuthorize("hasAuthority('PROFILE:READ')")`
- `updateProfile`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`
- `changePassword`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`

#### 5. `AuthenticationController` (`/api/v1/auth`)
- `logout`: `@PreAuthorize("isAuthenticated()")`
- `login`, `register`, `refresh`: Permitted publicly in `SecurityConfig`.

---

## 3. Testing & Verification Strategy

1. **`AuthenticationFilterTest`**: Verify `ROLE_ADMIN` and `USER:READ` mapping in `SecurityContextHolder`.
2. **Controller Security Tests**:
   - Verify `hasRole('ADMIN')` and `hasRole('SUPER_ADMIN')` expressions work properly.
   - Verify unauthenticated requests return 401 Unauthorized.
   - Verify missing permissions return 403 Forbidden.
3. **Full Build Verification**: Run `./mvnw clean test` to ensure 100% pass rate.
