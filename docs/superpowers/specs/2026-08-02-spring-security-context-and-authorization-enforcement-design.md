# Design Spec: Spring Security Context & Method-Level Authorization Enforcement

- **Date**: 2026-08-02
- **Topic**: Integration of roles and permissions into `AuthenticatedPrincipal` and `UserAuthenticationToken`, enabling `@EnableMethodSecurity`, and applying `@PreAuthorize` authorization controls across all system API endpoints.
- **SRS Reference**: [docs/srs.md](file:///home/maithehao/Workspace/projects/vaops/docs/srs.md) Section 5 & NFR-SEC-1

---

## 1. Overview & Purpose

This design completes the Spring Security integration for the `vaops` backend by:
1. Enhancing `AuthenticatedPrincipal` to store `roles` and `permissions` extracted from JWT Access Tokens.
2. Updating `AuthenticationFilter` to map claims directly into `SimpleGrantedAuthority` collections (raw strings, without prefixes like `ROLE_`) and attaching them to `UserAuthenticationToken` in `SecurityContextHolder`.
3. Enabling `@EnableMethodSecurity` in `SecurityConfig`.
4. Adding `@PreAuthorize` method security annotations across all existing controllers (`RoleController`, `PermissionController`, `UserRoleController`, `ProfileController`, `AuthenticationController`).

---

## 2. Component Specifications

### 2.1 Security Core Models (`shared.feature.security`)

#### 1. `AuthenticatedPrincipal`
Record definition update:
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

List<GrantedAuthority> authorities = Stream.concat(
    claims.roles().stream(),
    claims.permissions().stream()
)
.filter(str -> str != null && !str.isBlank())
.map(SimpleGrantedAuthority::new)
.collect(Collectors.toList());

UserAuthenticationToken authentication = new UserAuthenticationToken(principal, authorities);
authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
SecurityContextHolder.getContext().setAuthentication(authentication);
```

#### 3. `SecurityConfig` (`core.config`)
Annotate class with `@EnableMethodSecurity(prePostEnabled = true)`:
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
- `createRole`: `@PreAuthorize("hasAuthority('ROLE:CREATE')")`
- `updateRole`: `@PreAuthorize("hasAuthority('ROLE:UPDATE')")`
- `deleteRole`: `@PreAuthorize("hasAuthority('ROLE:DELETE')")`
- `getRoleById`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `searchRoles`: `@PreAuthorize("hasAuthority('ROLE:READ')")`
- `assignPermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION')")`
- `revokePermissions`: `@PreAuthorize("hasAuthority('ROLE:MANAGE_PERMISSION')")`

#### 2. `PermissionController` (`/api/v1/permissions`)
- `createPermission`: `@PreAuthorize("hasAuthority('PERMISSION:CREATE')")`
- `updatePermission`: `@PreAuthorize("hasAuthority('PERMISSION:UPDATE')")`
- `deletePermission`: `@PreAuthorize("hasAuthority('PERMISSION:DELETE')")`
- `getPermissionById`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`
- `searchPermissions`: `@PreAuthorize("hasAuthority('PERMISSION:READ')")`

#### 3. `UserRoleController` (`/api/v1/users/{userId}/roles`)
- `assignRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE')")`
- `revokeRoles`: `@PreAuthorize("hasAuthority('USER:MANAGE_ROLE')")`

#### 4. `ProfileController` (`/api/v1/profile`)
- `getProfile`: `@PreAuthorize("hasAuthority('PROFILE:READ')")`
- `updateProfile`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`
- `changePassword`: `@PreAuthorize("hasAuthority('PROFILE:UPDATE')")`

#### 5. `AuthenticationController` (`/api/v1/auth`)
- `logout`: `@PreAuthorize("isAuthenticated()")`
- `login`, `register`, `refresh`: Permitted publicly in `SecurityConfig`.

---

## 3. Testing & Verification Strategy

1. **`AuthenticationFilterTest`**: Verify `GrantedAuthority` mapping from token claims to `SecurityContextHolder`.
2. **Controller Security Tests**:
   - Verify unauthenticated requests return 401 Unauthorized.
   - Verify authenticated requests with missing permissions return 403 Forbidden.
   - Verify authenticated requests with required authorities pass successfully.
3. **Full Build Verification**: Run `./mvnw clean test` to ensure 100% pass rate.
