# Refresh Token Implementation Design Spec

Implement database-backed Refresh Token storage, validation, rotation, and lifecycle management to support secure session refreshes.

## Requirements & Policies

1. **Multi-device Session Support**:
   - Creating a new session (logging in) does NOT invalidate or revoke other active refresh tokens for the same user.
   - Users can be logged in from multiple devices/browsers simultaneously.
2. **Refresh Token Rotation (RTR)**:
   - When a client requests a token refresh using their active refresh token:
     - The old refresh token is marked as revoked in the database.
     - A new access token and a new refresh token are generated.
     - The new refresh token is hashed and saved in the database.
     - The response sets both access and refresh cookies.
3. **Defense-in-depth Hashing**:
   - Refresh tokens are cryptographic secrets. To prevent session hijacking in case of database leakage, we only store the SHA-256 hash of the refresh token in the database.
   - When verifying, we hash the received refresh token and lookup by its hash.

---

## Proposed Changes

### 1. Utility: Token Hash Utility
Create [TokenHashUtil](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/util/TokenHashUtil.java) to standardize token hashing.

- Method: `public static String hash(String token)`
  - Standard SHA-256 hash formatted as a hex string.

### 2. DTO Layer
Create command and result DTOs under `c4f.vannang.vaops.modules.authentication.api.dto`:
- [NEW] [RefreshTokenCommandDto.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/api/dto/RefreshTokenCommandDto.java):
  ```java
  package c4f.vannang.vaops.modules.authentication.api.dto;

  public record RefreshTokenCommandDto(String refreshToken) {}
  ```
- [NEW] [RefreshTokenCommandResultDto.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/api/dto/RefreshTokenCommandResultDto.java):
  ```java
  package c4f.vannang.vaops.modules.authentication.api.dto;

  public record RefreshTokenCommandResultDto(String accessToken, String refreshToken) {}
  ```

### 3. API & Implementations
Update the authentication module boundary:
- [MODIFY] [AuthenticationModuleApi.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/api/AuthenticationModuleApi.java):
  - Add: `RefreshTokenCommandResultDto refreshToken(RefreshTokenCommandDto command);`
- [MODIFY] [AuthenticationModuleApiImpl.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/service/AuthenticationModuleApiImpl.java):
  - Inject `RefreshTokenUseCase`.
  - Implement `refreshToken(RefreshTokenCommandDto)` delegating to `RefreshTokenUseCase`.

### 4. Repository Completion
Ensure [RefreshTokenQueryRepository.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/repository/RefreshTokenQueryRepository.java) compiles and supports:
- `Optional<RefreshToken> findByTokenHash(String tokenHash)`
- `Optional<RefreshToken> findByUserId(UUID userId)`

### 5. Use Cases

#### [MODIFY] [LoginUseCase.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/usecase/LoginUseCase.java)
- Inject `RefreshTokenWriteRepository` and `AuthProperties`.
- Save the newly generated refresh token in the database upon successful login:
  ```java
  String tokenHash = TokenHashUtil.hash(refreshToken);
  Instant expiredAt = Instant.now().plusMillis(authProperties.getJwt().getRefreshExpirationMs());
  RefreshToken entity = RefreshToken.create(userId, tokenHash, expiredAt);
  refreshTokenWriteRepository.save(entity);
  ```

#### [NEW] [RefreshTokenUseCase.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/internal/usecase/RefreshTokenUseCase.java)
- Inject `RefreshTokenQueryRepository`, `RefreshTokenWriteRepository`, `TokenProviderFactory`, `IdentityModuleApi`, and `AuthProperties`.
- Execute Flow:
  1. Validate JWT refresh token structure and signature:
     `RefreshTokenClaims claims = tokenService.validateRefreshToken(command.refreshToken());`
  2. Compute SHA-256 hash:
     `String tokenHash = TokenHashUtil.hash(command.refreshToken());`
  3. Fetch from DB:
     `RefreshToken oldToken = queryRepository.findByTokenHash(tokenHash).orElseThrow(() -> new UnauthenticatedException("Invalid refresh token"));`
  4. Verify validity:
     `if (!oldToken.isValid()) { throw new UnauthenticatedException("Invalid or revoked refresh token"); }`
  5. Check user status:
     `UserDto user = identityModuleApi.getUserById(new FindByIdQuery(claims.userId())).orElseThrow(() -> new UnauthenticatedException("User not found"));`
     `if (!user.active()) { throw new UnauthenticatedException("User account is inactive"); }`
  6. Apply token rotation:
     - Mark old token as revoked: `oldToken.revoke();` and save via `writeRepository.save(oldToken);`
     - Generate new access and refresh tokens via `TokenProviderStrategy`.
     - Hash the new refresh token and save it to the DB.
  7. Return `RefreshTokenCommandResultDto`.

### 6. Web Controller
[MODIFY] [AuthenticationController.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authentication/infrastructure/web/controller/AuthenticationController.java)
- Implement `POST /api/v1/auth/refresh`:
  - Retrieve the `refresh_token` from request cookies.
  - If null or empty, throw `UnauthenticatedException`.
  - Invoke `authModuleApi.refreshToken(new RefreshTokenCommandDto(refreshToken))`.
  - Issue the new access and refresh tokens as cookies in response headers.

---

## Verification Plan

### Automated Tests
- Create unit tests for `TokenHashUtil`.
- Create unit tests for `RefreshTokenUseCase` covering:
  - Success flow with rotation (revokes old token, saves new one, returns tokens).
  - Failure flow: Expired/Revoked token in DB.
  - Failure flow: Non-active user.
  - Failure flow: Tampered JWT token.
- Update `AuthServiceTest` or existing tests to match the updated constructors and services.
- Run `mvn test` to verify build correctness.
