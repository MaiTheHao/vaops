# Authorization API Package Implementation & JWT Token Claims Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the public `authorization.api` package (`RoleDto`, `PermissionDto`, `AuthorizationAPIService`, `AuthorizationApiMapper`, `PermissionUtils`), update `AccessTokenClaims` and `JwtAccessTokenProvider` to include roles and permissions, integrate into `AuthenticationServiceImpl`, and update the SRS walkthrough documentation.

**Architecture:** Inter-module contract port established via `authorization.api`. Authentication module queries active user roles & permissions via `AuthorizationAPIService`, formats permission strings using `PermissionUtils`, and serializes roles & permissions into JWT Access Token claims.

**Tech Stack:** Java 21, Spring Boot 3.x, MapStruct, jjwt (io.jsonwebtoken), JUnit 5, Mockito.

## Global Constraints

- **JPA Entities are Domain Entities**: Entities in `domain/` use JPA annotations directly.
- **Strict Package Separation**: `api` package contains public inter-module contracts (DTOs, Services, Mappers, Utils); `internal` contains private implementations and repositories.
- **Permission Formatting**: Use `PermissionUtils.format(...)` (`"RESOURCE:ACTION"`) for formatting permission strings.

---

## File Structure Map

```
backend/src/main/java/c4f/vannang/vaops/modules/authorization/
├── api/
│   ├── dto/
│   │   ├── RoleDto.java
│   │   └── PermissionDto.java
│   ├── mapper/
│   │   └── AuthorizationApiMapper.java
│   ├── service/
│   │   └── AuthorizationAPIService.java
│   └── util/
│       └── PermissionUtils.java
└── internal/
    ├── repository/
    │   └── PermissionQueryRepository.java (modified)
    └── service/impl/
        └── AuthorizationAPIServiceImpl.java

backend/src/main/java/c4f/vannang/vaops/shared/feature/token/
├── claims/
│   └── AccessTokenClaims.java (modified)
└── provider/
    └── JwtAccessTokenProvider.java (modified)

backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/service/impl/
└── AuthenticationServiceImpl.java (modified)

docs/srs.md (modified)
```

---

### Task 1: Create `authorization.api` DTOs and `PermissionUtils`

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/dto/RoleDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/dto/PermissionDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/util/PermissionUtils.java`
- Create Test: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/api/util/PermissionUtilsTest.java`

**Interfaces:**
- Produces: `RoleDto(String code, String description)`
- Produces: `PermissionDto(String resource, String action, String description)`
- Produces: `PermissionUtils.format(String, String)`, `PermissionUtils.format(PermissionDto)`, `PermissionUtils.parse(String)`

- [ ] **Step 1: Write the failing test for `PermissionUtils`**

Create file `backend/src/test/java/c4f/vannang/vaops/modules/authorization/api/util/PermissionUtilsTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.util;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionUtilsTest {

  @Test
  @DisplayName("Should format resource and action into RESOURCE:ACTION string")
  void testFormatString() {
    assertEquals("USER:READ", PermissionUtils.format("user", "read"));
    assertEquals("USER:WRITE", PermissionUtils.format(" USER ", " write "));
    assertEquals("", PermissionUtils.format(null, "read"));
  }

  @Test
  @DisplayName("Should format PermissionDto into RESOURCE:ACTION string")
  void testFormatDto() {
    PermissionDto dto = new PermissionDto("ROLE", "CREATE", "Create role");
    assertEquals("ROLE:CREATE", PermissionUtils.format(dto));
    assertEquals("", PermissionUtils.format(null));
  }

  @Test
  @DisplayName("Should parse RESOURCE:ACTION string into String array")
  void testParse() {
    String[] parts = PermissionUtils.parse("USER:READ");
    assertEquals("USER", parts[0]);
    assertEquals("READ", parts[1]);

    String[] emptyParts = PermissionUtils.parse("");
    assertEquals("", emptyParts[0]);
    assertEquals("", emptyParts[1]);
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run command:
`./mvnw test -Dtest=PermissionUtilsTest` (in `backend/` directory)
Expected: Compilation failure because `RoleDto`, `PermissionDto`, and `PermissionUtils` do not exist yet.

- [ ] **Step 3: Implement `RoleDto`, `PermissionDto`, and `PermissionUtils`**

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/dto/RoleDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.dto;

public record RoleDto(String code, String description) {}
```

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/dto/PermissionDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.dto;

public record PermissionDto(String resource, String action, String description) {}
```

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/util/PermissionUtils.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.util;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;

public final class PermissionUtils {

  private static final String DELIMITER = ":";

  private PermissionUtils() {}

  public static String format(String resource, String action) {
    if (resource == null || action == null) {
      return "";
    }
    return resource.strip().toUpperCase() + DELIMITER + action.strip().toUpperCase();
  }

  public static String format(PermissionDto permissionDto) {
    if (permissionDto == null) {
      return "";
    }
    return format(permissionDto.resource(), permissionDto.action());
  }

  public static String[] parse(String permissionString) {
    if (permissionString == null || permissionString.isBlank()) {
      return new String[] {"", ""};
    }
    String[] parts = permissionString.split(DELIMITER, 2);
    if (parts.length == 2) {
      return new String[] {parts[0].strip(), parts[1].strip()};
    }
    return new String[] {parts[0].strip(), ""};
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run command:
`./mvnw test -Dtest=PermissionUtilsTest` (in `backend/` directory)
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/
git add backend/src/test/java/c4f/vannang/vaops/modules/authorization/api/
git commit -m "feat(authorization): add RoleDto, PermissionDto, and PermissionUtils"
```

---

### Task 2: Implement `AuthorizationAPIService`, `AuthorizationApiMapper`, fix repository method name, and implement service

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/service/AuthorizationAPIService.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/mapper/AuthorizationApiMapper.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/impl/AuthorizationAPIServiceImpl.java`
- Create Test: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/internal/service/impl/AuthorizationAPIServiceImplTest.java`

**Interfaces:**
- Consumes: `RoleQueryRepository.findAllActiveByUserId`, `PermissionQueryRepository.findAllActiveByUserId`
- Produces: `AuthorizationAPIService` interface & `AuthorizationAPIServiceImpl` Spring service bean

- [ ] **Step 1: Fix `PermissionQueryRepository` method name**

In `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java`:
Rename `findActiveByUserId` to `findAllActiveByUserId`:
```java
  @Query("SELECT DISTINCT p FROM UserRole ur "
      + "JOIN Role r ON ur.id.roleId = r.id "
      + "JOIN RolePermission rp ON r.id = rp.id.roleId "
      + "JOIN Permission p ON rp.id.permissionId = p.id "
      + "WHERE ur.id.userId = :userId "
      + "AND r.active = true AND r.deletedAt IS NULL "
      + "AND p.active = true AND p.deletedAt IS NULL")
  List<Permission> findAllActiveByUserId(@Param("userId") UUID userId);
```

- [ ] **Step 2: Create interface `AuthorizationAPIService` and MapStruct Mapper `AuthorizationApiMapper`**

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/service/AuthorizationAPIService.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.service;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import java.util.List;
import java.util.UUID;

public interface AuthorizationAPIService {
  List<RoleDto> getRolesByUserId(UUID userId);
  List<PermissionDto> getPermissionsByUserId(UUID userId);
}
```

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/api/mapper/AuthorizationApiMapper.java`:
```java
package c4f.vannang.vaops.modules.authorization.api.mapper;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthorizationApiMapper {

  @Mapping(target = "code", expression = "java(role.getCode() != null ? role.getCode().value() : null)")
  @Mapping(target = "description", expression = "java(role.getDescription() != null ? role.getDescription().value() : null)")
  RoleDto toRoleDto(Role role);

  List<RoleDto> toRoleDtoList(List<Role> roles);

  @Mapping(target = "resource", expression = "java(permission.getResource() != null ? permission.getResource().value() : null)")
  @Mapping(target = "action", expression = "java(permission.getAction() != null ? permission.getAction().value() : null)")
  @Mapping(target = "description", expression = "java(permission.getDescription() != null ? permission.getDescription().value() : null)")
  PermissionDto toPermissionDto(Permission permission);

  List<PermissionDto> toPermissionDtoList(List<Permission> permissions);
}
```

- [ ] **Step 3: Write the failing unit test for `AuthorizationAPIServiceImpl`**

Create `backend/src/test/java/c4f/vannang/vaops/modules/authorization/internal/service/impl/AuthorizationAPIServiceImplTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.api.mapper.AuthorizationApiMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationAPIServiceImplTest {

  @Mock
  private RoleQueryRepository roleQueryRepository;

  @Mock
  private PermissionQueryRepository permissionQueryRepository;

  private AuthorizationApiMapper authorizationApiMapper;
  private AuthorizationAPIServiceImpl service;

  @BeforeEach
  void setUp() {
    authorizationApiMapper = Mappers.getMapper(AuthorizationApiMapper.class);
    service = new AuthorizationAPIServiceImpl(roleQueryRepository, permissionQueryRepository, authorizationApiMapper);
  }

  @Test
  @DisplayName("Should return active roles by user id")
  void testGetRolesByUserId() {
    UUID userId = UUID.randomUUID();
    Role role = Role.create(new RoleCode("ADMIN"), null);
    when(roleQueryRepository.findAllActiveByUserId(userId)).thenReturn(List.of(role));

    List<RoleDto> roles = service.getRolesByUserId(userId);

    assertEquals(1, roles.size());
    assertEquals("ADMIN", roles.get(0).code());
    verify(roleQueryRepository).findAllActiveByUserId(userId);
  }

  @Test
  @DisplayName("Should return active permissions by user id")
  void testGetPermissionsByUserId() {
    UUID userId = UUID.randomUUID();
    Permission permission = Permission.create(new PermissionResource("USER"), new PermissionAction("READ"), null);
    when(permissionQueryRepository.findAllActiveByUserId(userId)).thenReturn(List.of(permission));

    List<PermissionDto> permissions = service.getPermissionsByUserId(userId);

    assertEquals(1, permissions.size());
    assertEquals("USER", permissions.get(0).resource());
    assertEquals("READ", permissions.get(0).action());
    verify(permissionQueryRepository).findAllActiveByUserId(userId);
  }
}
```

- [ ] **Step 4: Run test to verify it fails**

Run command:
`./mvnw test -Dtest=AuthorizationAPIServiceImplTest` (in `backend/` directory)
Expected: FAIL because `AuthorizationAPIServiceImpl` is not created yet.

- [ ] **Step 5: Implement `AuthorizationAPIServiceImpl`**

Create `backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/impl/AuthorizationAPIServiceImpl.java`:
```java
package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import c4f.vannang.vaops.modules.authorization.api.dto.RoleDto;
import c4f.vannang.vaops.modules.authorization.api.mapper.AuthorizationApiMapper;
import c4f.vannang.vaops.modules.authorization.api.service.AuthorizationAPIService;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.RoleQueryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AuthorizationAPIServiceImpl implements AuthorizationAPIService {

  private final RoleQueryRepository roleQueryRepository;
  private final PermissionQueryRepository permissionQueryRepository;
  private final AuthorizationApiMapper authorizationApiMapper;

  @Override
  public List<RoleDto> getRolesByUserId(UUID userId) {
    List<Role> roles = roleQueryRepository.findAllActiveByUserId(userId);
    return authorizationApiMapper.toRoleDtoList(roles);
  }

  @Override
  public List<PermissionDto> getPermissionsByUserId(UUID userId) {
    List<Permission> permissions = permissionQueryRepository.findAllActiveByUserId(userId);
    return authorizationApiMapper.toPermissionDtoList(permissions);
  }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run command:
`./mvnw test -Dtest=AuthorizationAPIServiceImplTest` (in `backend/` directory)
Expected: PASS.

- [ ] **Step 7: Commit changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/
git add backend/src/test/java/c4f/vannang/vaops/modules/authorization/
git commit -m "feat(authorization): implement AuthorizationAPIService and update PermissionQueryRepository"
```

---

### Task 3: Update `AccessTokenClaims` & `JwtAccessTokenProvider` with claims testing

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/shared/feature/token/claims/AccessTokenClaims.java`
- Modify: `backend/src/main/java/c4f/vannang/vaops/shared/feature/token/provider/JwtAccessTokenProvider.java`
- Create Test: `backend/src/test/java/c4f/vannang/vaops/shared/feature/token/provider/JwtAccessTokenProviderTest.java`

**Interfaces:**
- Produces: Updated `AccessTokenClaims(UUID userId, String accountName, List<String> roles, List<String> permissions)`
- Produces: `JwtAccessTokenProvider` capable of encoding/decoding `roles` and `permissions` claims.

- [ ] **Step 1: Write failing test for `JwtAccessTokenProvider`**

Create `backend/src/test/java/c4f/vannang/vaops/shared/feature/token/provider/JwtAccessTokenProviderTest.java`:
```java
package c4f.vannang.vaops.shared.feature.token.provider;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtAccessTokenProviderTest {

  private JwtAccessTokenProvider provider;

  @BeforeEach
  void setUp() {
    AuthProperties properties = new AuthProperties();
    AuthProperties.Jwt jwt = new AuthProperties.Jwt();
    jwt.setAccessSecret("1234567890123456789012345678901234567890"); // 40+ chars secret
    jwt.setAccessExpirationMs(3600000L);
    jwt.setIssuer("vaops-test");
    properties.setJwt(jwt);

    provider = new JwtAccessTokenProvider(properties);
  }

  @Test
  @DisplayName("Should generate token with roles and permissions and parse them back")
  void testGenerateAndValidateWithRolesAndPermissions() {
    UUID userId = UUID.randomUUID();
    List<String> roles = List.of("ADMIN", "USER");
    List<String> permissions = List.of("USER:READ", "USER:WRITE");
    AccessTokenClaims claims = new AccessTokenClaims(userId, "john.doe", roles, permissions);

    String token = provider.generate(claims);
    assertNotNull(token);

    AccessTokenClaims validatedClaims = provider.validate(token);
    assertEquals(userId, validatedClaims.userId());
    assertEquals("john.doe", validatedClaims.accountName());
    assertEquals(roles, validatedClaims.roles());
    assertEquals(permissions, validatedClaims.permissions());
  }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run command:
`./mvnw test -Dtest=JwtAccessTokenProviderTest` (in `backend/` directory)
Expected: Compilation error or failure because `AccessTokenClaims` does not take `roles` and `permissions` arguments yet.

- [ ] **Step 3: Update `AccessTokenClaims.java`**

Modify `backend/src/main/java/c4f/vannang/vaops/shared/feature/token/claims/AccessTokenClaims.java`:
```java
package c4f.vannang.vaops.shared.feature.token.claims;

import java.util.List;
import java.util.UUID;

public record AccessTokenClaims(
    UUID userId,
    String accountName,
    List<String> roles,
    List<String> permissions
) implements TokenClaims {

  public AccessTokenClaims {
    roles = roles == null ? List.of() : roles;
    permissions = permissions == null ? List.of() : permissions;
  }

  public AccessTokenClaims(UUID userId, String accountName) {
    this(userId, accountName, List.of(), List.of());
  }
}
```

- [ ] **Step 4: Update `JwtAccessTokenProvider.java`**

Modify `backend/src/main/java/c4f/vannang/vaops/shared/feature/token/provider/JwtAccessTokenProvider.java`:
```java
package c4f.vannang.vaops.shared.feature.token.provider;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public final class JwtAccessTokenProvider implements AccessTokenSpec {

  private final SecretKey accessKey;
  private final AuthProperties authProperties;

  public JwtAccessTokenProvider(AuthProperties authProperties) {
    this.authProperties = authProperties;
    this.accessKey = Keys.hmacShaKeyFor(
        authProperties.getJwt().getAccessSecret().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generate(AccessTokenClaims claims) {
    Instant now = Instant.now();
    Instant expiry = now.plusMillis(authProperties.getJwt().getAccessExpirationMs());

    return Jwts.builder()
        .issuer(authProperties.getJwt().getIssuer())
        .subject(claims.accountName())
        .claim("userId", claims.userId().toString())
        .claim("roles", claims.roles())
        .claim("permissions", claims.permissions())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(accessKey)
        .compact();
  }

  @Override
  @SuppressWarnings("unchecked")
  public AccessTokenClaims validate(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();

      String userIdStr = claims.get("userId", String.class);
      if (userIdStr == null) {
        throw new UnauthenticatedException("Invalid token claims");
      }

      List<String> roles = claims.get("roles", List.class);
      List<String> permissions = claims.get("permissions", List.class);

      return new AccessTokenClaims(
          UUID.fromString(userIdStr),
          claims.getSubject(),
          roles != null ? roles : List.of(),
          permissions != null ? permissions : List.of()
      );
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Access token expired");
    } catch (Exception e) {
      throw new UnauthenticatedException("Invalid token");
    }
  }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run command:
`./mvnw test -Dtest=JwtAccessTokenProviderTest` (in `backend/` directory)
Expected: PASS.

- [ ] **Step 6: Commit changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/shared/feature/token/
git add backend/src/test/java/c4f/vannang/vaops/shared/feature/token/
git commit -m "feat(token): add roles and permissions claims to AccessTokenClaims and JwtAccessTokenProvider"
```

---

### Task 4: Integrate `AuthorizationAPIService` into `AuthenticationServiceImpl`

**Files:**
- Modify: `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/service/impl/AuthenticationServiceImpl.java`
- Modify / Create Test: `backend/src/test/java/c4f/vannang/vaops/modules/authentication/internal/service/impl/AuthenticationServiceImplTest.java` (or `AuthServiceTest.java`)

**Interfaces:**
- Consumes: `AuthorizationAPIService.getRolesByUserId`, `AuthorizationAPIService.getPermissionsByUserId`, `PermissionUtils.format`

- [ ] **Step 1: Inspect existing tests in authentication module**

Run:
`./mvnw test -Dtest=*Auth*` (in `backend/` directory)
Check which tests mock `AuthenticationServiceImpl` dependencies.

- [ ] **Step 2: Update `AuthenticationServiceImpl.java` to inject `AuthorizationAPIService`**

Modify `backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/service/impl/AuthenticationServiceImpl.java`:
1. Add field `private final AuthorizationAPIService authorizationAPIService;`
2. Update `login()` and `refreshToken()` helper methods to build claims with roles & permissions:

```java
  private AccessTokenClaims buildAccessTokenClaims(UUID userId, String accountName) {
    List<RoleDto> roleDtos = authorizationAPIService.getRolesByUserId(userId);
    List<PermissionDto> permDtos = authorizationAPIService.getPermissionsByUserId(userId);

    List<String> roles = roleDtos.stream().map(RoleDto::code).toList();
    List<String> permissions = permDtos.stream().map(PermissionUtils::format).toList();

    return new AccessTokenClaims(userId, accountName, roles, permissions);
  }
```

In `login(...)`:
Replace `AccessTokenClaims accessClaims = new AccessTokenClaims(userId, command.accountName());`
with `AccessTokenClaims accessClaims = buildAccessTokenClaims(userId, command.accountName());`.

In `refreshToken(...)`:
Replace `AccessTokenClaims accessClaims = new AccessTokenClaims(claims.userId(), user.accountName());`
with `AccessTokenClaims accessClaims = buildAccessTokenClaims(claims.userId(), user.accountName());`.

- [ ] **Step 3: Update authentication unit tests**

Update test classes under `backend/src/test/java/c4f/vannang/vaops/modules/authentication/` to mock `AuthorizationAPIService` returning empty lists (or sample roles/permissions), ensuring all tests pass.

- [ ] **Step 4: Run test suite to verify all auth tests pass**

Run command:
`./mvnw test -Dtest=*Auth*` (in `backend/` directory)
Expected: PASS.

- [ ] **Step 5: Commit changes**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authentication/
git add backend/src/test/java/c4f/vannang/vaops/modules/authentication/
git commit -m "feat(authentication): integrate AuthorizationAPIService to populate roles and permissions in access token claims"
```

---

### Task 5: Documentation Walkthrough Update in `docs/srs.md`

**Files:**
- Modify: `docs/srs.md`

- [ ] **Step 1: Update `docs/srs.md` Walkthrough Log**

At the bottom of `docs/srs.md`, update Section 9 (Open Items) / Section 7 (Decision Log) and add a **Completed Walkthrough Log** documenting:
1. Created package `c4f.vannang.vaops.modules.authorization.api` (`RoleDto`, `PermissionDto`, `AuthorizationAPIService`, `AuthorizationApiMapper`, `PermissionUtils`).
2. Renamed `PermissionQueryRepository.findActiveByUserId` -> `findAllActiveByUserId`.
3. Implemented `AuthorizationAPIServiceImpl` in `authorization.internal`.
4. Updated `AccessTokenClaims` and `JwtAccessTokenProvider` to support `roles` and `permissions`.
5. Integrated `AuthorizationAPIService` into `AuthenticationServiceImpl` for `login` and `refreshToken`.

- [ ] **Step 2: Commit documentation changes**

```bash
git add docs/srs.md
git commit -m "docs(srs): update walkthrough log with completed authorization api port and token claims integration"
```

---

## Verification Checklist & Full Build

Run full backend build & tests:
```bash
cd backend && ./mvnw clean test
```
Expected output: BUILD SUCCESS with zero test failures.
