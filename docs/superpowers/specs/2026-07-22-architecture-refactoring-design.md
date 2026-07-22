# Architecture Refactoring Design Specification: Core, Shared, and Modules

**Date:** 2026-07-22  
**Status:** Approved  
**Scope:** Backend (`c4f.vannang.vaops`)

---

## 1. Overview & Objectives

The primary goal of this refactoring is to establish a clean, maintainable, and loosely-coupled architecture across the top-level packages (`core`, `shared`, `modules`) in the `vaops` backend, following **Clean Architecture** and **Feature-based Packaging** principles.

### Key Architectural Principles & Dependency Rules
$$\text{core} \Longleftarrow \text{shared} \Longleftarrow \text{modules}$$

1. **`core` (Framework Bootstrap & HTTP Pipeline)**:
   - Contains application bootstrapping, environment configuration (`AuthProperties`), Security configuration (`SecurityConfig`), and HTTP Servlet Filters (`core.web.filter`).
   - **Strict Rule:** `core` MUST NOT import or depend on `shared` or `modules`.

2. **`shared` (Shared Features & Toolkit)**:
   - Organized by **Feature-based Packaging** (`token`, `crypto`, `security`, `exception`, `enumeration`).
   - Contains reusable domain models, token specifications, JWT providers, crypto strategies, and platform exceptions.
   - **Strict Rule:** `shared` depends on `core` (e.g., `JwtTokenProvider` uses `AuthProperties` from `core`), but MUST NOT depend on any class in `modules`.

3. **`modules` (Bounded Contexts)**:
   - Contains business domains and use cases (`authentication`, `identity`, `authorization`, `metric`).
   - `UserDetailsServiceImpl` (which queries `IdentityModuleApi`) is placed in `modules.authentication.infrastructure.security`.
   - **Strict Rule:** `modules` depends on `shared` and `core`.

---

## 2. Refactoring File Mapping

| Source Location | Target Location | Rationale & Responsibility |
| :--- | :--- | :--- |
| `modules.authentication.internal.config.AuthProperties` | `core.config.AuthProperties` | System-wide `@ConfigurationProperties("vaops.auth")` |
| `shared.filter.AuthenticationFilter` | `core.web.filter.AuthenticationFilter` | Web HTTP Authentication Middleware |
| `shared.filter.RequestLoggingFilter` | `core.web.filter.RequestLoggingFilter` | Web HTTP MDC Logging Middleware |
| `shared.filter.RequestTraceFilter` | `core.web.filter.RequestTraceFilter` | Web HTTP Trace ID Middleware |
| `shared.domain.security.UserDetailsServiceImpl` | `modules.authentication.infrastructure.security.UserDetailsServiceImpl` | Spring `UserDetailsService` implementation calling `IdentityModuleApi` |
| `shared.domain.security.AuthenticatedPrincipal` | `shared.security.AuthenticatedPrincipal` | Moved to Feature-based `shared.security` package |
| `shared.domain.crypto.*` | `shared.crypto.*` | Moved to Feature-based `shared.crypto` package |
| `shared.token.*` | `shared.token.*` | Kept as Feature package containing `claims`, `provider`, and `specification` |

---

## 3. Target Package Structure

```
c4f.vannang.vaops
│
├── core/                                    <--- LAYER 0: BOOTSTRAP & INFRASTRUCTURE
│   ├── config/
│   │   ├── AuthProperties.java              <--- Moved from modules.authentication
│   │   ├── FilterConfig.java
│   │   ├── PasswordEncoderConfig.java
│   │   └── SecurityConfig.java
│   └── web/
│       ├── advice/                          <--- GlobalExceptionHandler & PlatformExceptionHandler
│       └── filter/                          <--- Moved from shared.filter
│           ├── AuthenticationFilter.java
│           ├── RequestLoggingFilter.java
│           └── RequestTraceFilter.java
│
├── shared/                                  <--- LAYER 1: SHARED FEATURES & TOOLKIT
│   ├── crypto/                              <--- FEATURE: HASH & CRYPTO STRATEGIES
│   │   ├── DeterministicHashStrategy.java
│   │   ├── DeterministicHashStrategyFactory.java
│   │   ├── Sha256DeterministicHashStrategy.java
│   │   └── Sha512DeterministicHashStrategy.java
│   ├── enumeration/                         <--- FEATURE: SHARED ENUMS
│   │   ├── DeterministicHashAlgorithm.java
│   │   └── ErrorCode.java
│   ├── exception/                           <--- FEATURE: PLATFORM EXCEPTIONS
│   │   ├── AbstractPlatformException.java
│   │   └── ...
│   ├── security/                            <--- FEATURE: SECURITY PRINCIPAL MODEL
│   │   └── AuthenticatedPrincipal.java
│   └── token/                               <--- FEATURE: TOKEN SYSTEM
│       ├── claims/                          <--- AccessTokenClaims, RefreshTokenClaims...
│       ├── provider/                        <--- JwtTokenProvider, TokenProviderRegistry
│       └── specification/                   <--- AccessTokenSpec, RefreshTokenSpec...
│
└── modules/                                 <--- LAYER 2: BOUNDED CONTEXTS
    ├── authentication/
    │   ├── infrastructure/
    │   │   └── security/
    │   │       └── UserDetailsServiceImpl.java <--- Moved from shared.domain.security
    │   └── internal/
    │       ├── domain/
    │       ├── repository/
    │       └── usecase/
    ├── identity/
    ├── authorization/
    └── metric/
```

---

## 4. Verification & Testing Strategy

1. **Compilation Check**: Run `./mvnw clean compile` to ensure all package imports are correctly updated and there are no compilation errors.
2. **Unit & Integration Tests**: Run `./mvnw test` to verify that all authentication, token generation, and filter tests pass without regression.
3. **Dependency Boundary Verification**: Inspect imports to verify no `core` classes import `shared` or `modules`, and no `shared` classes import `modules`.
