# Architecture Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor backend top-level package boundaries (`core`, `shared`, `modules`) to enforce a clean dependency flow (`core` <= `shared` <= `modules`) and feature-based packaging within `shared`.

**Architecture:** Move HTTP Filters (`AuthenticationFilter`, `RequestLoggingFilter`, `RequestTraceFilter`) and `AuthProperties` to `core`. Move `UserDetailsServiceImpl` to `modules.authentication`. Re-organize `shared` into clean feature-based packages (`shared.crypto`, `shared.security`, `shared.token`, `shared.exception`, `shared.enumeration`).

**Tech Stack:** Java 21, Spring Boot 4.x, Maven, Spring Security.

## Global Constraints

- Respect dependency flow: `core` MUST NOT import `shared` or `modules`. `shared` MUST NOT import `modules`.
- Build must pass cleanly at every step (`mvn clean compile test`).
- Commit frequently after completing each task.

---

### Task 1: Move AuthProperties and HTTP Filters to Core

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/core/config/AuthProperties.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/core/web/filter/AuthenticationFilter.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/core/web/filter/RequestLoggingFilter.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/core/web/filter/RequestTraceFilter.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/core/config/FilterConfig.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/core/config/SecurityConfig.java`
- Delete: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/config/AuthProperties.java`
- Delete: `backend/src/main/java/c4f/vannang/vaops/shared/filter/AuthenticationFilter.java`
- Delete: `backend/src/main/java/c4f/vannang/vaops/shared/filter/RequestLoggingFilter.java`
- Delete: `backend/src/main/java/c4f/vannang/vaops/shared/filter/RequestTraceFilter.java`

- [ ] **Step 1: Create AuthProperties in core.config**

```java
package c4f.vannang.vaops.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String accessSecret;
        private long accessExpirationMs = 900_000;
        private String refreshSecret;
        private long refreshExpirationMs = 604_800_000;
        private String issuer = "vaops";
    }
}
```

- [ ] **Step 2: Move HTTP Filters to core.web.filter**

Move `AuthenticationFilter`, `RequestLoggingFilter`, and `RequestTraceFilter` to `package c4f.vannang.vaops.core.web.filter;`.

- [ ] **Step 3: Update FilterConfig and SecurityConfig imports**

Update `FilterConfig.java` and `SecurityConfig.java` to import filters from `c4f.vannang.vaops.core.web.filter.*`.

- [ ] **Step 4: Delete old files and update references to AuthProperties**

Delete old `AuthProperties.java` in `modules.authentication` and old filters in `shared.filter`. Update all references to `AuthProperties` across `shared.token.provider.JwtTokenProvider`, `LoginUseCase`, etc.

- [ ] **Step 5: Verify build**

Run: `cd backend && ./mvnw clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/core
git commit -m "refactor(core): move AuthProperties and HTTP filters to core package"
```

---

### Task 2: Move UserDetailsServiceImpl to Modules Authentication

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/security/UserDetailsServiceImpl.java`
- Delete: `backend/src/main/java/c4f/vannang/vaops/shared/domain/security/UserDetailsServiceImpl.java`

- [ ] **Step 1: Create UserDetailsServiceImpl in modules.authentication**

Create file at `backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/security/UserDetailsServiceImpl.java`:
```java
package c4f.vannang.vaops.modules.authentication.infrastructure.security;

import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final IdentityModuleApi identityModuleApi;

    @Override
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        UserAuthDto userAuth = identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + accountName));

        if (!userAuth.active()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
            throw new AccountLockedException("Account is locked until " + userAuth.lockedUntil());
        }

        return User.builder()
                .username(userAuth.id().toString())
                .password(userAuth.passwordHash())
                .authorities(Collections.emptyList())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!userAuth.active())
                .build();
    }
}
```

- [ ] **Step 2: Delete old UserDetailsServiceImpl in shared**

Remove `backend/src/main/java/c4f/vannang/vaops/shared/domain/security/UserDetailsServiceImpl.java`.

- [ ] **Step 3: Verify compilation**

Run: `cd backend && ./mvnw clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authentication
git rm backend/src/main/java/c4f/vannang/vaops/shared/domain/security/UserDetailsServiceImpl.java
git commit -m "refactor(auth): move UserDetailsServiceImpl to modules.authentication"
```

---

### Task 3: Refactor Shared Package to Feature-based Packages

**Files:**
- Move: `backend/src/main/java/c4f/vannang/vaops/shared/domain/crypto/*` -> `backend/src/main/java/c4f/vannang/vaops/shared/crypto/*`
- Move: `backend/src/main/java/c4f/vannang/vaops/shared/domain/security/AuthenticatedPrincipal.java` -> `backend/src/main/java/c4f/vannang/vaops/shared/security/AuthenticatedPrincipal.java`
- Remove empty directory: `backend/src/main/java/c4f/vannang/vaops/shared/domain`

- [ ] **Step 1: Move crypto classes to shared.crypto**

Change package declaration to `package c4f.vannang.vaops.shared.crypto;` for:
- `DeterministicHashStrategy.java`
- `DeterministicHashStrategyFactory.java`
- `Sha256DeterministicHashStrategy.java`
- `Sha512DeterministicHashStrategy.java`

- [ ] **Step 2: Move AuthenticatedPrincipal to shared.security**

Change package declaration to `package c4f.vannang.vaops.shared.security;` for `AuthenticatedPrincipal.java`.

- [ ] **Step 3: Update all imports across backend**

Search and replace import statements across all files in backend:
- `c4f.vannang.vaops.shared.domain.crypto.*` -> `c4f.vannang.vaops.shared.crypto.*`
- `c4f.vannang.vaops.shared.domain.security.AuthenticatedPrincipal` -> `c4f.vannang.vaops.shared.security.AuthenticatedPrincipal`

- [ ] **Step 4: Verify complete project build and tests**

Run: `cd backend && ./mvnw clean test`
Expected: BUILD SUCCESS and ALL TESTS PASS

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops
git commit -m "refactor(shared): organize shared package by feature (crypto, security)"
```
