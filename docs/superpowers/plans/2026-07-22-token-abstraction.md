# Token Abstraction & Domain-Centric Specification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor the token management system into a Clean Architecture domain-centric abstraction (`TokenSpecification<C>` & `TokenProviderRegistry`), decoupling `AuthenticationFilter` and authentication use-cases from internal token strategies.

**Architecture:** Business specifications (`AccessTokenSpec`, `RefreshTokenSpec`, etc.) and `TokenClaims` are placed in `shared.token`. Infrastructure providers like `JwtTokenProvider` implement these specs. `TokenProviderRegistry` manages providers and spring bean injection.

**Tech Stack:** Java 21, Spring Boot 3, JUnit 5, Mockito.

## Global Constraints

- Domain contracts and claims MUST reside under `c4f.vannang.vaops.shared.token`.
- No module inside `shared` or other business modules may import `c4f.vannang.vaops.modules.authentication.internal.*`.
- All tests MUST pass after each task execution (`./gradlew test`).

---

### Task 1: Create Shared Domain Claims & Specifications

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/claims/TokenClaims.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/claims/AccessTokenClaims.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/claims/RefreshTokenClaims.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/claims/EmailTokenClaims.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/claims/ResetPasswordTokenClaims.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/specification/TokenSpecification.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/specification/AccessTokenSpec.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/specification/RefreshTokenSpec.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/specification/EmailTokenSpec.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/specification/ResetPasswordTokenSpec.java`

**Interfaces:**
- Consumes: None
- Produces: `TokenClaims`, `AccessTokenClaims`, `RefreshTokenClaims`, `EmailTokenClaims`, `ResetPasswordTokenClaims`, `TokenSpecification<C>`, `AccessTokenSpec`, `RefreshTokenSpec`, `EmailTokenSpec`, `ResetPasswordTokenSpec`

- [ ] **Step 1: Write Domain Claims & Specification Interfaces**

Create `TokenClaims.java`:
```java
package c4f.vannang.vaops.shared.token.claims;

public interface TokenClaims {
}
```

Create `AccessTokenClaims.java`:
```java
package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record AccessTokenClaims(UUID userId, String accountName) implements TokenClaims {
}
```

Create `RefreshTokenClaims.java`:
```java
package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record RefreshTokenClaims(UUID userId) implements TokenClaims {
}
```

Create `EmailTokenClaims.java`:
```java
package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record EmailTokenClaims(UUID userId, String email) implements TokenClaims {
}
```

Create `ResetPasswordTokenClaims.java`:
```java
package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record ResetPasswordTokenClaims(UUID userId, String accountName) implements TokenClaims {
}
```

Create `TokenSpecification.java`:
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.TokenClaims;

public interface TokenSpecification<C extends TokenClaims> {
    String generate(C claims);
    C validate(String token);
}
```

Create `AccessTokenSpec.java`:
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;

public interface AccessTokenSpec extends TokenSpecification<AccessTokenClaims> {
}
```

Create `RefreshTokenSpec.java`:
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;

public interface RefreshTokenSpec extends TokenSpecification<RefreshTokenClaims> {
}
```

Create `EmailTokenSpec.java`:
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.EmailTokenClaims;

public interface EmailTokenSpec extends TokenSpecification<EmailTokenClaims> {
}
```

Create `ResetPasswordTokenSpec.java`:
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.ResetPasswordTokenClaims;

public interface ResetPasswordTokenSpec extends TokenSpecification<ResetPasswordTokenClaims> {
}
```

- [ ] **Step 2: Compile to verify Java syntax**

Run: `./gradlew compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/token/
git commit -m "feat(shared): add TokenClaims and TokenSpecification contracts"
```

---

### Task 2: Create TokenProvider Marker & TokenProviderRegistry

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/provider/TokenProvider.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/token/provider/TokenProviderRegistry.java`
- Test: `backend/src/test/java/c4f/vannang/vaops/shared/token/provider/TokenProviderRegistryTest.java`

**Interfaces:**
- Consumes: None
- Produces: `TokenProvider`, `TokenProviderRegistry` (methods: `get(String)`, `jwt()`, `paseto()`, `redis()`)

- [ ] **Step 1: Write TokenProvider interface and TokenProviderRegistry**

Create `TokenProvider.java`:
```java
package c4f.vannang.vaops.shared.token.provider;

public interface TokenProvider {
    String getName();
}
```

Create `TokenProviderRegistry.java`:
```java
package c4f.vannang.vaops.shared.token.provider;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TokenProviderRegistry {
    private final Map<String, TokenProvider> providers;

    public TokenProviderRegistry(List<TokenProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        p -> p.getName().toLowerCase(),
                        p -> p,
                        (existing, replacement) -> existing));
    }

    public TokenProvider get(String providerName) {
        return Optional.ofNullable(providers.get(providerName.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("No TokenProvider registered for name: " + providerName));
    }

    public TokenProvider jwt() {
        return get("jwt");
    }

    public TokenProvider paseto() {
        return get("paseto");
    }

    public TokenProvider redis() {
        return get("redis");
    }
}
```

- [ ] **Step 2: Write failing unit test for TokenProviderRegistry**

Create `TokenProviderRegistryTest.java`:
```java
package c4f.vannang.vaops.shared.token.provider;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderRegistryTest {

    static class DummyJwtProvider implements TokenProvider {
        @Override
        public String getName() {
            return "jwt";
        }
    }

    @Test
    void testGetProviderByName() {
        DummyJwtProvider jwtProvider = new DummyJwtProvider();
        TokenProviderRegistry registry = new TokenProviderRegistry(List.of(jwtProvider));

        assertEquals(jwtProvider, registry.get("jwt"));
        assertEquals(jwtProvider, registry.jwt());
    }

    @Test
    void testGetUnknownProviderThrowsException() {
        TokenProviderRegistry registry = new TokenProviderRegistry(List.of());

        assertThrows(IllegalArgumentException.class, () -> registry.get("unknown"));
    }
}
```

- [ ] **Step 3: Run test to verify it passes**

Run: `./gradlew test --tests c4f.vannang.vaops.shared.token.provider.TokenProviderRegistryTest`
Expected: BUILD SUCCESSFUL (PASSED)

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/token/provider/ backend/src/test/java/c4f/vannang/vaops/shared/token/provider/
git commit -m "feat(shared): add TokenProvider marker and TokenProviderRegistry"
```

---

### Task 3: Implement JwtTokenProvider & Deprecate Legacy Strategy

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/shared/infrastructure/token/jwt/JwtTokenProvider.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/JwtTokenProviderService.java` (or replace with JwtTokenProvider)
- Remove/Clean up: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/TokenProviderStrategy.java`
- Remove/Clean up: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/TokenProviderFactory.java`

**Interfaces:**
- Consumes: `TokenProvider`, `AccessTokenSpec`, `RefreshTokenSpec`, `AccessTokenClaims`, `RefreshTokenClaims`
- Produces: `JwtTokenProvider` bean implementing `TokenProvider`, `AccessTokenSpec`, `RefreshTokenSpec`

- [ ] **Step 1: Create JwtTokenProvider in shared infrastructure**

Create `JwtTokenProvider.java`:
```java
package c4f.vannang.vaops.shared.infrastructure.token.jwt;

import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;
import c4f.vannang.vaops.shared.token.provider.TokenProvider;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import c4f.vannang.vaops.shared.token.specification.RefreshTokenSpec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider, AccessTokenSpec, RefreshTokenSpec {

    private final SecretKey key;
    private final AuthProperties authProperties;

    public JwtTokenProvider(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.key = Keys.hmacShaKeyFor(authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return "jwt";
    }

    @Override
    public String generate(AccessTokenClaims claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(authProperties.getJwt().getAccessTokenExpirationMinutes(), java.time.temporal.ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(claims.accountName())
                .claim("userId", claims.userId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    @Override
    public AccessTokenClaims validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userIdStr = claims.get("userId", String.class);
            if (userIdStr == null) {
                throw new UnauthenticatedException("Invalid token claims");
            }
            return new AccessTokenClaims(UUID.fromString(userIdStr), claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Access token expired");
        } catch (Exception e) {
            throw new UnauthenticatedException("Invalid token");
        }
    }

    @Override
    public String generate(RefreshTokenClaims claims) {
        Instant now = Instant.now();
        Instant expiry = now.plus(authProperties.getJwt().getRefreshTokenExpirationDays(), java.time.temporal.ChronoUnit.DAYS);

        return Jwts.builder()
                .subject(claims.userId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    @Override
    public RefreshTokenClaims validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new RefreshTokenClaims(UUID.fromString(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Refresh token expired");
        } catch (Exception e) {
            throw new UnauthenticatedException("Invalid refresh token");
        }
    }
}
```

- [ ] **Step 2: Remove legacy TokenProviderStrategy, TokenProviderFactory, and JwtTokenProviderService**

Remove old internal files:
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/TokenProviderStrategy.java`
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/TokenProviderFactory.java`
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/JwtTokenProviderService.java`
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/dto/AccessTokenClaims.java`
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/dto/RefreshTokenClaims.java`
- `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/enumeration/TokenType.java`

- [ ] **Step 3: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/
git commit -m "feat(infrastructure): implement JwtTokenProvider and remove legacy internal token strategy"
```

---

### Task 4: Refactor AuthenticationFilter & Use Cases

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/shared/filter/AuthenticationFilter.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/usecase/LoginUseCase.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/usecase/RefreshTokenUseCase.java`
- Modify: `backend/src/test/java/c4f/vannang/vaops/modules/authentication/internal/usecase/LoginUseCaseTest.java`
- Modify: `backend/src/test/java/c4f/vannang/vaops/modules/authentication/internal/usecase/RefreshTokenUseCaseTest.java`

- [ ] **Step 1: Refactor AuthenticationFilter**

Update `AuthenticationFilter.java` to inject `AccessTokenSpec`:
```java
package c4f.vannang.vaops.shared.filter;

import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenSpec accessTokenSpec;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(AccessTokenSpec accessTokenSpec,
            UserDetailsService userDetailsService,
            ObjectMapper objectMapper) {
        this.accessTokenSpec = accessTokenSpec;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                AccessTokenClaims claims = accessTokenSpec.validate(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(claims.accountName());

                AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                        UUID.fromString(userDetails.getUsername()),
                        claims.accountName());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, e);
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

    private void writeErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (e instanceof TokenExpiredException) {
            response.setStatus(401);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 401,
                    "code", "TOKEN_EXPIRED",
                    "message", e.getMessage()));
        } else if (e instanceof AccountLockedException) {
            response.setStatus(423);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 423,
                    "code", "ACCOUNT_LOCKED",
                    "message", e.getMessage()));
        } else {
            response.setStatus(401);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 401,
                    "code", "AUTHENTICATION_FAILED",
                    "message", "Invalid or expired token"));
        }
    }
}
```

- [ ] **Step 2: Update LoginUseCase & RefreshTokenUseCase to use AccessTokenSpec & RefreshTokenSpec**

Update `LoginUseCase.java`:
- Replace `TokenProviderFactory` / `TokenProviderStrategy` with `AccessTokenSpec` and `RefreshTokenSpec`.
- Use `accessTokenSpec.generate(new AccessTokenClaims(user.id(), user.accountName()))` and `refreshTokenSpec.generate(new RefreshTokenClaims(user.id()))`.

Update `RefreshTokenUseCase.java`:
- Use `refreshTokenSpec.validate(command.refreshToken())` and `accessTokenSpec.generate(new AccessTokenClaims(...))`.

- [ ] **Step 3: Update Unit Tests**

Update imports in `LoginUseCaseTest.java` and `RefreshTokenUseCaseTest.java` to use `AccessTokenSpec`, `RefreshTokenSpec`, `AccessTokenClaims`, `RefreshTokenClaims` from `c4f.vannang.vaops.shared.token.*`.

- [ ] **Step 4: Run full test suite to verify everything passes**

Run: `./gradlew test`
Expected: BUILD SUCCESSFUL (All tests pass)

- [ ] **Step 5: Commit**

```bash
git add backend/src/
git commit -m "refactor(auth): update AuthenticationFilter and use cases to use AccessTokenSpec and RefreshTokenSpec"
```
