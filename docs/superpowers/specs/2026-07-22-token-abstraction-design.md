# Token Abstraction & Domain-Centric Specification Design

## Executive Summary
This design document defines a Clean Architecture / DDD compliant Token Abstraction for the system. Instead of treating JWT as the core domain concept, the architecture centers on **Business Token Specifications** (`AccessTokenSpec`, `RefreshTokenSpec`, `EmailTokenSpec`, `ResetPasswordTokenSpec`). Technology implementations (JWT, PASETO, Redis) act purely as Infrastructure Providers implementing these business specifications.

This decoupling eliminates the tight coupling between `AuthenticationFilter` (and other shared modules) and the internal implementations of the `authentication` module.

---

## 1. Architecture Overview

```text
                                        Application / Security Filter
                                                     │
                                   generate / verify Access Token
                                                     │
                                                     ▼
                                         TokenProviderRegistry
                                                     │
                              choose provider by configuration / runtime
                 ┌───────────────────────────────────┼────────────────────────────────────┐
                 │                                   │                                    │
                 ▼                                   ▼                                    ▼
           JwtTokenProvider                 PasetoTokenProvider                  RedisTokenProvider
                 │                                   │                                    │
                 │ implements                        │ implements                         │ implements
                 │                                   │                                    │
                 ├──────────────┐                    ├──────────────┐                     ├──────────────┐
                 ▼              ▼                    ▼              ▼                     ▼              ▼
        AccessTokenSpec   RefreshTokenSpec    AccessTokenSpec   RefreshTokenSpec    AccessTokenSpec   SessionTokenSpec
                 ▲
                 │
                 │ extends
                 │
          TokenSpecification<AccessTokenClaims>
```

---

## 2. Package & Class Hierarchy

### 2.1 Package Structure
Target package path in `backend`:

```text
c4f.vannang.vaops.shared.token
│
├── claims
│   ├── TokenClaims.java
│   ├── AccessTokenClaims.java
│   ├── RefreshTokenClaims.java
│   ├── EmailTokenClaims.java
│   └── ResetPasswordTokenClaims.java
│
├── specification
│   ├── TokenSpecification.java
│   ├── AccessTokenSpec.java
│   ├── RefreshTokenSpec.java
│   ├── EmailTokenSpec.java
│   └── ResetPasswordTokenSpec.java
│
├── provider
│   ├── TokenProvider.java
│   └── TokenProviderRegistry.java
│
└── exception (uses shared token exceptions)
```

Target infrastructure package path:

```text
c4f.vannang.vaops.shared.infrastructure.token.jwt
│
└── JwtTokenProvider.java
```

---

### 2.2 Domain Interfaces & Classes

#### `TokenClaims` (Marker Interface)
```java
package c4f.vannang.vaops.shared.token.claims;

public interface TokenClaims {
}
```

#### Claims Implementations (Records)
```java
package c4f.vannang.vaops.shared.token.claims;

import java.util.UUID;

public record AccessTokenClaims(UUID userId, String accountName) implements TokenClaims {}

public record RefreshTokenClaims(UUID userId) implements TokenClaims {}

public record EmailTokenClaims(UUID userId, String email) implements TokenClaims {}

public record ResetPasswordTokenClaims(UUID userId, String accountName) implements TokenClaims {}
```

#### `TokenSpecification<C extends TokenClaims>`
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.TokenClaims;

public interface TokenSpecification<C extends TokenClaims> {
    String generate(C claims);
    C validate(String token);
}
```

#### Business Contracts (`*TokenSpec`)
```java
package c4f.vannang.vaops.shared.token.specification;

import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.claims.EmailTokenClaims;
import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;
import c4f.vannang.vaops.shared.token.claims.ResetPasswordTokenClaims;

public interface AccessTokenSpec extends TokenSpecification<AccessTokenClaims> {}
public interface RefreshTokenSpec extends TokenSpecification<RefreshTokenClaims> {}
public interface EmailTokenSpec extends TokenSpecification<EmailTokenClaims> {}
public interface ResetPasswordTokenSpec extends TokenSpecification<ResetPasswordTokenClaims> {}
```

---

### 2.3 Provider Abstraction & Registry

#### `TokenProvider`
```java
package c4f.vannang.vaops.shared.token.provider;

public interface TokenProvider {
    String getName();
}
```

#### `TokenProviderRegistry`
```java
package c4f.vannang.vaops.shared.token.provider;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class TokenProviderRegistry {
    private final Map<String, TokenProvider> providers;

    public TokenProviderRegistry(List<TokenProvider> providerList) {
        // Collect registered TokenProviders by name
    }

    public TokenProvider get(String providerName) { ... }
    public TokenProvider jwt() { return get("jwt"); }
    public TokenProvider paseto() { return get("paseto"); }
    public TokenProvider redis() { return get("redis"); }
}
```

---

### 2.4 Infrastructure Implementation (`JwtTokenProvider`)

`JwtTokenProvider` implements `TokenProvider`, `AccessTokenSpec`, `RefreshTokenSpec`, `EmailTokenSpec`, `ResetPasswordTokenSpec`.

```java
package c4f.vannang.vaops.shared.infrastructure.token.jwt;

import c4f.vannang.vaops.shared.token.provider.TokenProvider;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import c4f.vannang.vaops.shared.token.specification.RefreshTokenSpec;
// ... imports ...

@Component
public class JwtTokenProvider implements TokenProvider, AccessTokenSpec, RefreshTokenSpec {
    @Override
    public String getName() {
        return "jwt";
    }

    @Override
    public String generate(AccessTokenClaims claims) { ... }

    @Override
    public AccessTokenClaims validate(String token) { ... }

    @Override
    public String generate(RefreshTokenClaims claims) { ... }

    @Override
    public RefreshTokenClaims validate(String token) { ... }
}
```

---

### 2.5 Refactoring `AuthenticationFilter` & Use Cases

`AuthenticationFilter` is updated to inject `AccessTokenSpec` directly (or retrieve it via `TokenProviderRegistry`), eliminating all imports from `c4f.vannang.vaops.modules.authentication.internal.*`.

```java
package c4f.vannang.vaops.shared.filter;

import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
// ... other imports ...

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    private final AccessTokenSpec accessTokenSpec;

    public AuthenticationFilter(AccessTokenSpec accessTokenSpec, ...) {
        this.accessTokenSpec = accessTokenSpec;
        ...
    }

    @Override
    protected void doFilterInternal(...) {
        ...
        AccessTokenClaims claims = accessTokenSpec.validate(token);
        ...
    }
}
```

Similarly, UseCases inside `modules.authentication` (such as `LoginUseCase`, `RefreshTokenUseCase`) will use `AccessTokenSpec` and `RefreshTokenSpec` contracts instead of internal token strategies.

---

## 3. Benefits & Trade-offs

| Aspect | Description |
| --- | --- |
| **Domain Centric** | Business rules define token specifications, not JWT libraries. |
| **Loose Coupling** | `AuthenticationFilter` and `shared` modules depend only on abstractions in `shared.token`. |
| **Flexibility** | Providers implement only the token specs they support. New providers (PASETO, Redis) can be added without modifying domain contracts. |
| **Type Safety** | Strong compile-time checking for `Claims` objects per specification. |

---

## 4. Verification Plan

1. **Compilation**: Run `./gradlew compileJava` or `./gradlew build -x test` to verify all interfaces, claims, registry, and provider implementations compile cleanly.
2. **Unit Tests**: Run backend tests (`./gradlew test`) to verify `LoginUseCaseTest`, `RefreshTokenUseCaseTest`, and `AuthenticationFilterTest` pass without regressions.
