# Authorization Infrastructure Web Package Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the `authorization.infrastructure.web` package providing REST API endpoints for managing Roles, Permissions, and User-Role assignments, complete with MapStruct mappings, Bean Validation, soft/hard delete handling, and tests.

**Architecture:** Create sub-domain REST controllers (`RoleController`, `PermissionController`, `UserRoleController`) operating over internal services (`RoleService`, `PermissionService`, `UserRoleService`). An `AuthorizationWebMapper` MapStruct interface converts between Web DTOs, internal commands/criteria, and domain entities.

**Tech Stack:** Java 21, Spring Boot 3, Spring Web, Jakarta Bean Validation, MapStruct, JUnit 5, Mockito, MockMvc.

## Global Constraints

- Package root: `c4f.vannang.vaops.modules.authorization.infrastructure.web`
- Standard response page wrapper: `c4f.vannang.vaops.shared.dto.PageResponse`
- Security principal annotation: `@AuthenticationPrincipal AuthenticatedPrincipal principal`
- Soft-delete query parameter: `?hard=false` (default `false`), `?hard=true` for hard-delete
- MapStruct annotation: `@Mapper(componentModel = "spring")`

---

### Task 1: Web Request and Response DTOs

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/CreateRoleWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/UpdateRoleWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/AssignPermissionsRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/RevokePermissionsRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/CreatePermissionWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/UpdatePermissionWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/AssignRolesToUserWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/request/RevokeRoleFromUserWebRequestDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/response/PermissionWebResponseDto.java`
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/response/RoleWebResponseDto.java`

**Interfaces:**
- Consumes: Jakarta Validation annotations (`@NotBlank`, `@Size`, `@NotEmpty`)
- Produces: Web Request & Response Java record DTOs for Controllers and Mapper

- [ ] **Step 1: Write Role Web Request DTOs**

Write `CreateRoleWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;

public record CreateRoleWebRequestDto(
    @NotBlank @Size(max = 256) String code,
    @Size(max = 1024) String description,
    Set<UUID> permissionIds
) {}
```

Write `UpdateRoleWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleWebRequestDto(
    @NotBlank @Size(max = 256) String code,
    @Size(max = 1024) String description
) {}
```

Write `AssignPermissionsRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

public record AssignPermissionsRequestDto(
    @NotEmpty Set<UUID> permissionIds
) {}
```

Write `RevokePermissionsRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

public record RevokePermissionsRequestDto(
    @NotEmpty Set<UUID> permissionIds
) {}
```

- [ ] **Step 2: Write Permission Web Request DTOs**

Write `CreatePermissionWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePermissionWebRequestDto(
    @NotBlank @Size(max = 256) String resource,
    @NotBlank @Size(max = 256) String action,
    @Size(max = 1024) String description
) {}
```

Write `UpdatePermissionWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePermissionWebRequestDto(
    @NotBlank @Size(max = 256) String resource,
    @NotBlank @Size(max = 256) String action,
    @Size(max = 1024) String description
) {}
```

- [ ] **Step 3: Write User-Role Web Request DTOs**

Write `AssignRolesToUserWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

public record AssignRolesToUserWebRequestDto(
    @NotEmpty Set<UUID> roleIds
) {}
```

Write `RevokeRoleFromUserWebRequestDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.UUID;

public record RevokeRoleFromUserWebRequestDto(
    @NotEmpty Set<UUID> roleIds
) {}
```

- [ ] **Step 4: Write Response DTOs**

Write `PermissionWebResponseDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PermissionWebResponseDto(
    UUID id,
    String code,
    String resource,
    String action,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {}
```

Write `RoleWebResponseDto.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoleWebResponseDto(
    UUID id,
    String code,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt,
    List<PermissionWebResponseDto> permissions
) {}
```

- [ ] **Step 5: Verify Compilation**

Run: `./mvnw clean compile -pl backend`
Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/dto/
git commit -m "feat(authorization): add web request and response DTOs with bean validation"
```

---

### Task 2: Authorization Web MapStruct Mapper and Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/mapper/AuthorizationWebMapper.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/mapper/AuthorizationWebMapperTest.java`

**Interfaces:**
- Consumes: Web DTOs, internal commands (`CreateRoleCommand`, `UpdateRoleCommand`, `AssignPermissionsToRoleCommand`, `RevokePermissionFromRoleCommand`, `CreatePermissionCommand`, `UpdatePermissionCommand`, `AssignRolesToUserCommand`, `RevokeRoleFromUserCommand`, `RoleSearchCriteria`, `PermissionSearchCriteria`), domain entities (`Role`, `Permission`, `RolePermission`).
- Produces: Spring bean `AuthorizationWebMapper`.

- [ ] **Step 1: Write failing unit test for AuthorizationWebMapper**

Write `AuthorizationWebMapperTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.*;
import c4f.vannang.vaops.modules.authorization.internal.domain.*;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.*;
import c4f.vannang.vaops.modules.authorization.internal.dto.*;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AuthorizationWebMapperTest {

  private AuthorizationWebMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = Mappers.getMapper(AuthorizationWebMapper.class);
  }

  @Test
  void shouldMapCreateRoleRequestToCommand() {
    UUID permId = UUID.randomUUID();
    var request = new CreateRoleWebRequestDto("ROLE_ADMIN", "Admin role", Set.of(permId));

    CreateRoleCommand command = mapper.toCreateRoleCommand(request);

    assertThat(command).isNotNull();
    assertThat(command.code()).isEqualTo("ROLE_ADMIN");
    assertThat(command.description()).isEqualTo("Admin role");
    assertThat(command.permissionIds()).containsExactly(permId);
  }

  @Test
  void shouldMapUpdateRoleRequestToCommand() {
    UUID roleId = UUID.randomUUID();
    var request = new UpdateRoleWebRequestDto("ROLE_SUPER_ADMIN", "Super Admin");

    UpdateRoleCommand command = mapper.toUpdateRoleCommand(roleId, request);

    assertThat(command).isNotNull();
    assertThat(command.id()).isEqualTo(roleId);
    assertThat(command.code()).isEqualTo("ROLE_SUPER_ADMIN");
    assertThat(command.description()).isEqualTo("Super Admin");
  }

  @Test
  void shouldMapCreatePermissionRequestToCommand() {
    var request = new CreatePermissionWebRequestDto("USER", "READ", "Read user permission");

    CreatePermissionCommand command = mapper.toCreatePermissionCommand(request);

    assertThat(command).isNotNull();
    assertThat(command.resource()).isEqualTo("USER");
    assertThat(command.action()).isEqualTo("READ");
    assertThat(command.description()).isEqualTo("Read user permission");
  }

  @Test
  void shouldMapRoleEntityToResponseDto() {
    Role role = Role.create(RoleCode.of("ROLE_USER"), "User role");

    RoleWebResponseDto response = mapper.toRoleWebResponseDto(role);

    assertThat(response).isNotNull();
    assertThat(response.code()).isEqualTo("ROLE_USER");
    assertThat(response.description()).isEqualTo("User role");
    assertThat(response.active()).isTrue();
  }

  @Test
  void shouldMapPermissionEntityToResponseDto() {
    Permission permission = Permission.create(
        PermissionResource.of("DOCUMENT"),
        PermissionAction.of("WRITE"),
        PermissionDescription.of("Write documents")
    );

    PermissionWebResponseDto response = mapper.toPermissionWebResponseDto(permission);

    assertThat(response).isNotNull();
    assertThat(response.code()).isEqualTo("DOCUMENT:WRITE");
    assertThat(response.resource()).isEqualTo("DOCUMENT");
    assertThat(response.action()).isEqualTo("WRITE");
    assertThat(response.description()).isEqualTo("Write documents");
    assertThat(response.active()).isTrue();
  }
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./mvnw test -pl backend -Dtest=AuthorizationWebMapperTest`
Expected: Compilation failure because `AuthorizationWebMapper` does not exist yet.

- [ ] **Step 3: Write implementation for AuthorizationWebMapper**

Write `AuthorizationWebMapper.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper;

import c4f.vannang.vaops.modules.authorization.api.util.PermissionUtils;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.*;
import c4f.vannang.vaops.modules.authorization.internal.domain.*;
import c4f.vannang.vaops.modules.authorization.internal.dto.*;
import c4f.vannang.vaops.shared.dto.PageResponse;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuthorizationWebMapper {

  CreateRoleCommand toCreateRoleCommand(CreateRoleWebRequestDto dto);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "code", source = "dto.code")
  @Mapping(target = "description", source = "dto.description")
  UpdateRoleCommand toUpdateRoleCommand(UUID id, UpdateRoleWebRequestDto dto);

  @Mapping(target = "roleId", source = "roleId")
  @Mapping(target = "permissionIds", source = "dto.permissionIds")
  AssignPermissionsToRoleCommand toAssignPermissionsToRoleCommand(UUID roleId, AssignPermissionsRequestDto dto);

  @Mapping(target = "roleId", source = "roleId")
  @Mapping(target = "permissionIds", source = "dto.permissionIds")
  RevokePermissionFromRoleCommand toRevokePermissionFromRoleCommand(UUID roleId, RevokePermissionsRequestDto dto);

  CreatePermissionCommand toCreatePermissionCommand(CreatePermissionWebRequestDto dto);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "resource", source = "dto.resource")
  @Mapping(target = "action", source = "dto.action")
  @Mapping(target = "description", source = "dto.description")
  UpdatePermissionCommand toUpdatePermissionCommand(UUID id, UpdatePermissionWebRequestDto dto);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "roleIds", source = "dto.roleIds")
  @Mapping(target = "assignedBy", source = "assignedBy")
  AssignRolesToUserCommand toAssignRolesToUserCommand(UUID userId, AssignRolesToUserWebRequestDto dto, UUID assignedBy);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "roleIds", source = "dto.roleIds")
  @Mapping(target = "revokedBy", source = "revokedBy")
  RevokeRoleFromUserCommand toRevokeRoleFromUserCommand(UUID userId, RevokeRoleFromUserWebRequestDto dto, UUID revokedBy);

  RoleSearchCriteria toRoleSearchCriteria(
      String keyword,
      String code,
      Boolean isActive,
      UUID userId,
      Instant createdFrom,
      Instant createdTo,
      int page,
      int size,
      String sortBy,
      String sortDirection
  );

  PermissionSearchCriteria toPermissionSearchCriteria(
      String keyword,
      String resource,
      String action,
      Boolean isActive,
      Collection<UUID> roleIds,
      Instant createdFrom,
      Instant createdTo,
      int page,
      int size,
      String sortBy,
      String sortDirection
  );

  @Mapping(target = "code", expression = "java(role.getCode() != null ? role.getCode().value() : null)")
  @Mapping(target = "permissions", source = "rolePermissions", qualifiedByName = "mapRolePermissions")
  RoleWebResponseDto toRoleWebResponseDto(Role role);

  List<RoleWebResponseDto> toRoleWebResponseDtoList(List<Role> roles);

  @Mapping(target = "code", expression = "java(formatPermissionCode(permission))")
  @Mapping(target = "resource", expression = "java(permission.getResource() != null ? permission.getResource().value() : null)")
  @Mapping(target = "action", expression = "java(permission.getAction() != null ? permission.getAction().value() : null)")
  @Mapping(target = "description", expression = "java(permission.getDescription() != null ? permission.getDescription().value() : null)")
  PermissionWebResponseDto toPermissionWebResponseDto(Permission permission);

  List<PermissionWebResponseDto> toPermissionWebResponseDtoList(List<Permission> permissions);

  default PageResponse<RoleWebResponseDto> toRolePageResponse(PageResponse<Role> page) {
    if (page == null) return null;
    return new PageResponse<>(
        toRoleWebResponseDtoList(page.items()),
        page.page(),
        page.size(),
        page.totalItems(),
        page.totalPages(),
        page.isFirst(),
        page.isLast()
    );
  }

  default PageResponse<PermissionWebResponseDto> toPermissionPageResponse(PageResponse<Permission> page) {
    if (page == null) return null;
    return new PageResponse<>(
        toPermissionWebResponseDtoList(page.items()),
        page.page(),
        page.size(),
        page.totalItems(),
        page.totalPages(),
        page.isFirst(),
        page.isLast()
    );
  }

  @Named("mapRolePermissions")
  default List<PermissionWebResponseDto> mapRolePermissions(java.util.Set<RolePermission> rolePermissions) {
    if (rolePermissions == null) return List.of();
    return rolePermissions.stream()
        .filter(rp -> rp.getPermission() != null)
        .map(rp -> toPermissionWebResponseDto(rp.getPermission()))
        .toList();
  }

  default String formatPermissionCode(Permission permission) {
    if (permission == null || permission.getResource() == null || permission.getAction() == null) {
      return null;
    }
    return PermissionUtils.format(permission.getResource().value(), permission.getAction().value());
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -pl backend -Dtest=AuthorizationWebMapperTest`
Expected: `BUILD SUCCESS` with tests passing.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/mapper/ AuthorizationWebMapper.java backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/mapper/AuthorizationWebMapperTest.java
git commit -m "feat(authorization): implement AuthorizationWebMapper and mapper unit tests"
```

---

### Task 3: Role Controller & MockMvc Integration Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/RoleController.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/RoleControllerTest.java`

**Interfaces:**
- Consumes: `RoleService`, `AuthorizationWebMapper`
- Produces: REST Endpoints at `/api/v1/roles`

- [ ] **Step 1: Write failing controller test for RoleController**

Write `RoleControllerTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.UpdateRoleWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import c4f.vannang.vaops.modules.authorization.internal.service.RoleService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private RoleService roleService;
  @MockBean private AuthorizationWebMapper webMapper;

  @Test
  void createRole_shouldReturn201_whenValid() throws Exception {
    var dto = new CreateRoleWebRequestDto("ROLE_MANAGER", "Manager role", null);
    Role role = Role.create(RoleCode.of("ROLE_MANAGER"), "Manager role");

    given(webMapper.toCreateRoleCommand(any())).willReturn(null);
    given(roleService.createRole(any())).willReturn(role);

    mockMvc.perform(post("/api/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void createRole_shouldReturn400_whenCodeIsBlank() throws Exception {
    var dto = new CreateRoleWebRequestDto("", "Invalid role", null);

    mockMvc.perform(post("/api/v1/roles")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void deleteRole_shouldCallSoftDelete_whenHardIsFalse() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/roles/{id}", id))
        .andExpect(status().isNoContent());

    verify(roleService).softDeleteRole(eq(id), any());
  }

  @Test
  void deleteRole_shouldCallHardDelete_whenHardIsTrue() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/roles/{id}", id).param("hard", "true"))
        .andExpect(status().isNoContent());

    verify(roleService).hardDeleteRole(id);
  }

  @Test
  void getRoleById_shouldReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    Role role = Role.create(RoleCode.of("ROLE_ADMIN"), "Admin");
    given(roleService.getRoleById(id)).willReturn(role);

    mockMvc.perform(get("/api/v1/roles/{id}", id))
        .andExpect(status().isOk());
  }
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./mvnw test -pl backend -Dtest=RoleControllerTest`
Expected: Failure because `RoleController` does not exist yet.

- [ ] **Step 3: Write implementation for RoleController**

Write `RoleController.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Role;
import c4f.vannang.vaops.modules.authorization.internal.dto.*;
import c4f.vannang.vaops.modules.authorization.internal.service.RoleService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

  private final RoleService roleService;
  private final AuthorizationWebMapper webMapper;

  @PostMapping
  public ResponseEntity<RoleWebResponseDto> createRole(
      @Valid @RequestBody CreateRoleWebRequestDto request) {
    CreateRoleCommand command = webMapper.toCreateRoleCommand(request);
    Role role = roleService.createRole(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toRoleWebResponseDto(role));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RoleWebResponseDto> updateRole(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateRoleWebRequestDto request) {
    UpdateRoleCommand command = webMapper.toUpdateRoleCommand(id, request);
    Role role = roleService.updateRole(command);
    return ResponseEntity.ok(webMapper.toRoleWebResponseDto(role));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteRole(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "false") boolean hard,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    if (hard) {
      roleService.hardDeleteRole(id);
    } else {
      UUID deletedBy = principal != null ? principal.userId() : null;
      roleService.softDeleteRole(id, deletedBy);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<RoleWebResponseDto> getRoleById(@PathVariable UUID id) {
    Role role = roleService.getRoleById(id);
    return ResponseEntity.ok(webMapper.toRoleWebResponseDto(role));
  }

  @GetMapping
  public ResponseEntity<PageResponse<RoleWebResponseDto>> searchRoles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) UUID userId,
      @RequestParam(required = false) Instant createdFrom,
      @RequestParam(required = false) Instant createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "code") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    RoleSearchCriteria criteria = webMapper.toRoleSearchCriteria(
        keyword, code, isActive, userId, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<Role> result = roleService.searchRoles(criteria);
    return ResponseEntity.ok(webMapper.toRolePageResponse(result));
  }

  @PostMapping("/{roleId}/permissions")
  public ResponseEntity<Void> assignPermissions(
      @PathVariable UUID roleId,
      @Valid @RequestBody AssignPermissionsRequestDto request) {
    AssignPermissionsToRoleCommand command = webMapper.toAssignPermissionsToRoleCommand(roleId, request);
    roleService.assignPermissionsToRole(command);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{roleId}/permissions")
  public ResponseEntity<Void> revokePermissions(
      @PathVariable UUID roleId,
      @Valid @RequestBody RevokePermissionsRequestDto request) {
    RevokePermissionFromRoleCommand command = webMapper.toRevokePermissionFromRoleCommand(roleId, request);
    roleService.unassignPermissionsFromRole(command);
    return ResponseEntity.ok().build();
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -pl backend -Dtest=RoleControllerTest`
Expected: `BUILD SUCCESS` with tests passing.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/RoleController.java backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/RoleControllerTest.java
git commit -m "feat(authorization): add RoleController REST endpoints and controller tests"
```

---

### Task 4: Permission Controller & MockMvc Integration Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/PermissionController.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/PermissionControllerTest.java`

**Interfaces:**
- Consumes: `PermissionService`, `AuthorizationWebMapper`
- Produces: REST Endpoints at `/api/v1/permissions`

- [ ] **Step 1: Write failing controller test for PermissionController**

Write `PermissionControllerTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.CreatePermissionWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PermissionControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private PermissionService permissionService;
  @MockBean private AuthorizationWebMapper webMapper;

  @Test
  void createPermission_shouldReturn201_whenValid() throws Exception {
    var dto = new CreatePermissionWebRequestDto("USER", "READ", "Read user");
    Permission perm = Permission.create(
        PermissionResource.of("USER"),
        PermissionAction.of("READ"),
        PermissionDescription.of("Read user")
    );

    given(permissionService.createPermission(any())).willReturn(perm);

    mockMvc.perform(post("/api/v1/permissions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated());
  }

  @Test
  void deletePermission_shouldCallSoftDelete_whenHardIsFalse() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/permissions/{id}", id))
        .andExpect(status().isNoContent());

    verify(permissionService).softDeletePermission(eq(id), any());
  }

  @Test
  void deletePermission_shouldCallHardDelete_whenHardIsTrue() throws Exception {
    UUID id = UUID.randomUUID();

    mockMvc.perform(delete("/api/v1/permissions/{id}", id).param("hard", "true"))
        .andExpect(status().isNoContent());

    verify(permissionService).hardDeletePermission(id);
  }
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./mvnw test -pl backend -Dtest=PermissionControllerTest`
Expected: Failure because `PermissionController` does not exist yet.

- [ ] **Step 3: Write implementation for PermissionController**

Write `PermissionController.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.response.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.*;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionService permissionService;
  private final AuthorizationWebMapper webMapper;

  @PostMapping
  public ResponseEntity<PermissionWebResponseDto> createPermission(
      @Valid @RequestBody CreatePermissionWebRequestDto request) {
    CreatePermissionCommand command = webMapper.toCreatePermissionCommand(request);
    Permission permission = permissionService.createPermission(command);
    return ResponseEntity.status(HttpStatus.CREATED).body(webMapper.toPermissionWebResponseDto(permission));
  }

  @PutMapping("/{id}")
  public ResponseEntity<PermissionWebResponseDto> updatePermission(
      @PathVariable UUID id,
      @Valid @RequestBody UpdatePermissionWebRequestDto request) {
    UpdatePermissionCommand command = webMapper.toUpdatePermissionCommand(id, request);
    Permission permission = permissionService.updatePermission(command);
    return ResponseEntity.ok(webMapper.toPermissionWebResponseDto(permission));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePermission(
      @PathVariable UUID id,
      @RequestParam(defaultValue = "false") boolean hard,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    if (hard) {
      permissionService.hardDeletePermission(id);
    } else {
      UUID deletedBy = principal != null ? principal.userId() : null;
      permissionService.softDeletePermission(id, deletedBy);
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<PermissionWebResponseDto> getPermissionById(@PathVariable UUID id) {
    Permission permission = permissionService.getPermissionById(id);
    return ResponseEntity.ok(webMapper.toPermissionWebResponseDto(permission));
  }

  @GetMapping
  public ResponseEntity<PageResponse<PermissionWebResponseDto>> searchPermissions(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String resource,
      @RequestParam(required = false) String action,
      @RequestParam(required = false) Boolean isActive,
      @RequestParam(required = false) Collection<UUID> roleIds,
      @RequestParam(required = false) Instant createdFrom,
      @RequestParam(required = false) Instant createdTo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "resource") String sortBy,
      @RequestParam(defaultValue = "ASC") String sortDirection) {
    PermissionSearchCriteria criteria = webMapper.toPermissionSearchCriteria(
        keyword, resource, action, isActive, roleIds, createdFrom, createdTo, page, size, sortBy, sortDirection);
    PageResponse<Permission> result = permissionService.searchPermissions(criteria);
    return ResponseEntity.ok(webMapper.toPermissionPageResponse(result));
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -pl backend -Dtest=PermissionControllerTest`
Expected: `BUILD SUCCESS` with tests passing.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/PermissionController.java backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/PermissionControllerTest.java
git commit -m "feat(authorization): add PermissionController REST endpoints and controller tests"
```

---

### Task 5: User-Role Controller & Integration Tests

**Files:**
- Create: `backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/UserRoleController.java`
- Create: `backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/UserRoleControllerTest.java`

**Interfaces:**
- Consumes: `UserRoleService`, `AuthorizationWebMapper`
- Produces: REST Endpoints at `/api/v1/users/{userId}/roles`

- [ ] **Step 1: Write failing controller test for UserRoleController**

Write `UserRoleControllerTest.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.AssignRolesToUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.RevokeRoleFromUserWebRequestDto;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserRoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserRoleControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserRoleService userRoleService;
  @MockBean private AuthorizationWebMapper webMapper;

  @Test
  void assignRolesToUser_shouldReturn200() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    var dto = new AssignRolesToUserWebRequestDto(Set.of(roleId));

    mockMvc.perform(post("/api/v1/users/{userId}/roles", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(userRoleService).assignRolesToUser(any());
  }

  @Test
  void revokeRoleFromUser_shouldReturn200() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    var dto = new RevokeRoleFromUserWebRequestDto(Set.of(roleId));

    mockMvc.perform(delete("/api/v1/users/{userId}/roles", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk());

    verify(userRoleService).unAssignRolesFromUser(any());
  }
}
```

- [ ] **Step 2: Run test to verify failure**

Run: `./mvnw test -pl backend -Dtest=UserRoleControllerTest`
Expected: Failure because `UserRoleController` does not exist yet.

- [ ] **Step 3: Write implementation for UserRoleController**

Write `UserRoleController.java`:
```java
package c4f.vannang.vaops.modules.authorization.infrastructure.web.controller;

import c4f.vannang.vaops.modules.authorization.infrastructure.web.dto.request.*;
import c4f.vannang.vaops.modules.authorization.infrastructure.web.mapper.AuthorizationWebMapper;
import c4f.vannang.vaops.modules.authorization.internal.dto.*;
import c4f.vannang.vaops.modules.authorization.internal.service.UserRoleService;
import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
@RequiredArgsConstructor
public class UserRoleController {

  private final UserRoleService userRoleService;
  private final AuthorizationWebMapper webMapper;

  @PostMapping
  public ResponseEntity<Void> assignRolesToUser(
      @PathVariable UUID userId,
      @Valid @RequestBody AssignRolesToUserWebRequestDto request,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    UUID assignedBy = principal != null ? principal.userId() : null;
    AssignRolesToUserCommand command = webMapper.toAssignRolesToUserCommand(userId, request, assignedBy);
    userRoleService.assignRolesToUser(command);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> revokeRoleFromUser(
      @PathVariable UUID userId,
      @Valid @RequestBody RevokeRoleFromUserWebRequestDto request,
      @AuthenticationPrincipal AuthenticatedPrincipal principal) {
    UUID revokedBy = principal != null ? principal.userId() : null;
    RevokeRoleFromUserCommand command = webMapper.toRevokeRoleFromUserCommand(userId, request, revokedBy);
    userRoleService.unAssignRolesFromUser(command);
    return ResponseEntity.ok().build();
  }
}
```

- [ ] **Step 4: Run full test suite for authorization module**

Run: `./mvnw test -pl backend -Dtest=*Authorization*Test,*RoleControllerTest,*PermissionControllerTest,*UserRoleControllerTest`
Expected: `BUILD SUCCESS` with 100% tests passing.

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/UserRoleController.java backend/src/test/java/c4f/vannang/vaops/modules/authorization/infrastructure/web/controller/UserRoleControllerTest.java
git commit -m "feat(authorization): add UserRoleController REST endpoints and controller tests"
```

---

### Task 6: Comprehensive Verification & Test Suite Execution

**Files:**
- Test all: Run total backend test suite

- [ ] **Step 1: Run complete Maven test suite**

Run: `./mvnw clean test -pl backend`
Expected: `BUILD SUCCESS` with zero test failures across the entire backend.

- [ ] **Step 2: Commit final documentation update if needed**

```bash
git add docs/superpowers/plans/2026-08-02-authorization-web-infrastructure.md
git commit -m "docs: finalize authorization web infrastructure implementation plan"
```
