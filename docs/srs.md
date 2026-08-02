# SRS — Module Authorization

> **Phạm vi tài liệu:** SRS này chỉ mô tả **module `authorization`** trong hệ thống `vaops` (backend). Không bao phủ toàn hệ thống. Tài liệu tối ưu cho LLM đọc (cấu trúc rõ ràng, ID truy vết, decision log).
>
> **Bản chất:** SRS **to-be** — mô tả contract cho 2 package `api` và `infrastructure` **sẽ được triển khai** (hiện tại cả 2 đều trống/không tồn tại), dựa trên domain nội bộ (`internal`) đã có.

## 1. Giới thiệu

### 1.1 Mục đích
Mô tả yêu cầu chức năng (FR) và phi chức năng (NFR) cho module `authorization`, tập trung vào 2 package sẽ triển khai:
- `api` — **public port contract** giữa các module (inter-module), tối thiểu để build `AuthenticatedPrincipal`.
- `infrastructure` — **public web contract** cho end-user / Frontend (FE) qua REST (quản trị role/permission).

### 1.2 Phạm vi
- **Trong phạm vi:** package `api` và `infrastructure` của module `authorization` (to-be).
- **Ngoài phạm vi:** logic domain nội bộ (`internal`), module `authentication`, `identity`, toàn hệ thống.

### 1.3 Định nghĩa / Thuật ngữ
| Thuật ngữ | Định nghĩa |
|---|---|
| Role | Nhóm quyền, gán cho user (entity `Role`). |
| Permission | Quyền cụ thể = `resource` + `action` (entity `Permission`). |
| UserRole | Quan hệ user ↔ role (join table `user_roles`). |
| RolePermission | Quan hệ role ↔ permission (join table `role_permissions`). |
| AuthenticatedPrincipal | Đối tượng xác thực trong Spring Security, chứa roles/permissions của user. |
| Soft-delete | Bản ghi bị đánh dấu `deletedAt != null`, vẫn còn trong DB nhưng bị ẩn khỏi truy vấn mặc định (BaseSoftDeletableEntity). |
| Deactivate | Vô hiệu hóa bản ghi bằng `active = false` (Activatable) — tách biệt với soft-delete. |
| Mapper | MapStruct mapper (`@Mapper(componentModel = "spring")`). **Mỗi package có mapper riêng**, đóng vai trò serialize input từ package khác → DTO chuẩn của package đó (anti-corruption layer). Ví dụ: `internal/mapper` map `api.dto` → `internal.dto`; `api/mapper` map input từ package khác → `api.dto`. |
| Hard-delete | Xóa hẳn bản ghi khỏi DB. |

## 2. Kiến trúc & Vai trò package (to-be)

```
c4f.vannang.vaops.modules.authorization
├── api/                    # PUBLIC PORT — contract inter-module (TO-BE)
│   ├── dto/                # RoleDto, PermissionDto (đơn giản)
│   ├── mapper/             # ApiMapper — map input từ package khác → api.dto (TO-BE)
│   └── service/            # AuthorizationAPIService (interface)
├── infrastructure/         # PUBLIC WEB — REST cho FE (TO-BE)
│   └── web/
│       ├── controller/     # RoleController, PermissionController, UserRoleController
│       └── dto/            # Web DTO + Bean validation
└── internal/               # (ngoài phạm vi SRS) domain, service, repository — ĐÃ CÓ
    └── mapper/              # AuthorizationMapper (MapStruct) — TO-BE (hiện trống)
```

| Package | Vai trò | Người tiêu thụ |
|---|---|---|
| `api` | Port contract inter-module, đọc roles/permissions của user | Module khác (vd `authentication`), Spring Security |
| `infrastructure/web` | REST quản trị role/permission | Frontend admin |

## 3. Yêu cầu chức năng — Public API port (`api`)

### 3.1 `AuthorizationAPIService` (interface) — TO-BE
Port contract tối thiểu để build `AuthenticatedPrincipal`:
- `List<RoleDto> getRolesByUserId(UUID userId)`
- `List<PermissionDto> getPermissionsByUserId(UUID userId)`

### 3.2 DTO (api.dto) — TO-BE
| DTO | Fields |
|---|---|
| `RoleDto` | `code` (String), `description` (String) |
| `PermissionDto` | `resource` (String), `action` (String), `description` (String) |

### 3.3 FR-API
- **FR-API-1:** `getRolesByUserId(userId)` trả danh sách role **active** của user (code, description).
- **FR-API-2:** `getPermissionsByUserId(userId)` trả danh sách permission **active** của user (resource, action, description).
- **FR-API-3:** Dữ liệu lấy từ `internal` service/repository hiện có (`RoleQueryRepository.findAllActiveByUserId`, `PermissionQueryRepository.findActiveByUserId`).
- **FR-API-4:** DTO đơn giản (không kèm id/timestamps) — đủ cho principal.

### 3.4 Mapper — TO-BE (mỗi package có mapper riêng)
- **FR-API-5:** Mỗi package có mapper riêng (MapStruct, `@Mapper(componentModel = "spring")`), đóng vai trò serialize input từ package khác → DTO chuẩn của package đó.
- **FR-API-6:** `internal/mapper` (`AuthorizationMapper`): map `api.dto` → `internal.dto` (command/criteria), method `toInternal(...)`. Theo pattern `identity/internal/mapper/IdentityMapper.java`.
- **FR-API-7:** `api/mapper` (`AuthorizationApiMapper`): map input từ package khác (module khác / web) → `api.dto`. Hiện trống, cần tạo khi triển khai `api`.
- **FR-API-8:** `infrastructure/web` cũng có mapper riêng map web DTO → DTO chuẩn của package đích.

## 4. Yêu cầu chức năng — REST Web (`infrastructure/web`)

### 4.1 Endpoints — Roles (`/api/v1/roles`)
| Method | Path | Request | Response | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/roles` | `CreateRoleWebRequestDto` | 201 `RoleWebResponseDto` | Tạo role |
| PUT | `/api/v1/roles/{id}` | `UpdateRoleWebRequestDto` | 200 `RoleWebResponseDto` | Cập nhật role |
| DELETE | `/api/v1/roles/{id}?hard=true` | — | 204 | Xóa (mặc định soft; `hard=true` → hard) |
| GET | `/api/v1/roles/{id}` | — | 200 `RoleWebResponseDto` | Lấy role theo id |
| GET | `/api/v1/roles` | query params | 200 `PageResponse<RoleWebResponseDto>` | Search + phân trang |
| POST | `/api/v1/roles/{roleId}/permissions` | `AssignPermissionsRequestDto` | 200 | Gán permission vào role |
| DELETE | `/api/v1/roles/{roleId}/permissions` | `RevokePermissionsRequestDto` | 200 | Gỡ permission khỏi role |

### 4.2 Endpoints — Permissions
| Method | Path | Request | Response | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/permissions` | `CreatePermissionWebRequestDto` | 201 `PermissionWebResponseDto` | Tạo permission |
| PUT | `/api/v1/permissions/{id}` | `UpdatePermissionWebRequestDto` | 200 | Cập nhật permission |
| DELETE | `/api/v1/permissions/{id}?hard=true` | — | 204 | Soft/hard delete |
| GET | `/api/v1/permissions/{id}` | — | 200 | Chi tiết permission |
| GET | `/api/v1/permissions` | query params | 200 `PageResponse<PermissionWebResponseDto>` | Search + phân trang |

### 4.3 Endpoints — User-Role
| Method | Path | Request | Response | Mô tả |
|---|---|---|---|---|
| POST | `/api/v1/users/{userId}/roles` | `AssignRolesToUserWebRequestDto` | 200 | Gán role cho user |
| DELETE | `/api/v1/users/{userId}/roles` | `RevokeRoleFromUserWebRequestDto` | 200 | Revoke role khỏi user |

### 4.4 FR-WEB (nghiệp vụ)
- **FR-WEB-1:** Tạo role: code unique; trùng code → `ResourceAlreadyExistsException`.
- **FR-WEB-2:** Tạo permission: cặp `resource`+`action` unique; trùng → `ResourceAlreadyExistsException`.
- **FR-WEB-3:** Update role/permission: không tìm thấy → `ResourceNotFoundException`.
- **FR-WEB-4:** Delete mặc định **soft-delete** (đặt `deletedAt != null`); `?hard=true` → hard delete (xóa hẳn). Deactivate (`active = false`) là thao tác riêng, không đồng nhất với soft-delete.
- **FR-WEB-5:** Assign/revoke permission vào role; assign role cho user; revoke role khỏi user.
- **FR-WEB-6:** Search roles theo `keyword, code, isActive, userId, createdFrom, createdTo, page, size, sortBy, sortDirection`.
- **FR-WEB-7:** Search permissions theo `keyword, resource, action, isActive, roleIds, createdFrom, createdTo, page, size, sortBy, sortDirection`.
- **FR-WEB-8:** Search trả `PageResponse` chuẩn (shared.dto).

### 4.5 Validation (web DTO) — Bean Validation
- Dùng `@NotBlank`, `@Size` trên web DTO (giống module `authentication`).
- `code`, `resource`, `action`: `@NotBlank` + `@Size(max=256)`.
- `description`: `@Size(max=1024)` (nullable).

## 5. Tích hợp Spring Security / AuthenticatedPrincipal (ngữ cảnh)

- **FR-SEC-INT-1:** `api` port là **nguồn dữ liệu** để build `AuthenticatedPrincipal` sau khi xác thực (từ `authentication` module).
- **FR-SEC-INT-2:** `getRolesByUserId` + `getPermissionsByUserId` được gọi để nạp roles/permissions vào principal.
- **FR-SEC-INT-3:** Spring Security dùng principal để check quyền (vd `@PreAuthorize("hasAuthority(...)")`).
- **FR-SEC-INT-4:** Permission check (`hasPermission`) **chỉ qua api port**, không expose REST.

## 6. Yêu cầu phi chức năng (NFR)

### 6.1 Bảo mật
- **NFR-SEC-1:** Chỉ user có quyền quản trị mới gọi được REST admin (`/api/v1/roles`, `/api/v1/permissions`, `/api/v1/users/{id}/roles`).
- **NFR-SEC-2:** Soft-delete (`deletedAt != null`) mặc định để tránh mất dữ liệu; hard-delete chỉ khi chủ động. Deactivate (`active = false`) là trạng thái riêng biệt.

### 6.2 Hiệu năng & giao dịch
- **NFR-REL-1:** Các thao tác ghi (create/update/delete/assign/revoke) là `@Transactional`.
- **NFR-REL-2:** Các thao tác đọc (get/search) là `@Transactional(readOnly=true)`.

### 6.3 Khác
- **NFR-ERR-1:** Exception domain (Validation, ResourceNotFound, ResourceAlreadyExists) được rethrow; lỗi khác bọc `InternalServerException`.
- **NFR-VER-1:** Versioning qua URL prefix `/api/v1`.

## 7. Ràng buộc & Quyết định (Decision Log)

| ID | Quyết định | Ghi chú |
|---|---|---|
| D1 | SRS **to-be** cho `api` + `infrastructure` (hiện trống) | Theo xác nhận người dùng |
| D2 | `api` port tối thiểu: `getRolesByUserId` + `getPermissionsByUserId` | Chỉ đủ build AuthenticatedPrincipal |
| D3 | Permission check chỉ qua api port, không expose REST | |
| D4 | REST tách riêng `/api/v1/roles`, `/api/v1/permissions`, `/api/v1/users/{userId}/roles` | Không dùng prefix `/authorization` |
| D5 | Delete mặc định soft; `?hard=true` → hard | |
| D6 | Search expose, trả `PageResponse` chuẩn | |
| D7 | Validation dùng Bean Validation `@NotBlank`/`@Size` | |
| D8 | DTO api port đơn giản (code/resource/action) | |
| D9 | Mô tả tích hợp Spring Security / AuthenticatedPrincipal | |

## 8. Truy vết (Traceability)
| Yêu cầu | Nguồn (file) |
|---|---|
| FR-API-1..4 | `internal/service/RoleService.java`, `PermissionService.java`, `RoleQueryRepository`, `PermissionQueryRepository` |
| FR-WEB-1..8 | `internal/service/*`, `internal/dto/*`, `internal/repository/spec/*` |
| NFR-SEC-1..2 | `internal/domain/Role.java`, `Permission.java` (Activatable) |
| NFR-REL-1..2 | `internal/service/impl/*` |

## 9. Vấn đề mở & Trạng thái triển khai (Implementation Status)

- [x] **API Package Port (`authorization.api`) — HOÀN THÀNH**:
  - `RoleDto`, `PermissionDto` (`api.dto`).
  - `PermissionUtils` (`api.util`): `format` & `parse` chuỗi permission `RESOURCE:ACTION`.
  - `AuthorizationAPIService` (`api.service`): `getRolesByUserId(UUID)`, `getPermissionsByUserId(UUID)`.
  - `AuthorizationApiMapper` (`api.mapper`): MapStruct mapper cho `api.dto`.
  - `AuthorizationAPIServiceImpl` (`internal.service.impl`).
  - Tích hợp vào `AuthenticationServiceImpl` (nạp `roles` & `permissions` vào `AccessTokenClaims` và `JwtAccessTokenProvider`).
- [ ] **Infrastructure Web Package (`authorization.infrastructure.web`) — CHƯA TRIỂN KHẢI (PENDING)**:
  - Controller & DTOs quản trị Roles (`/api/v1/roles`).
  - Controller & DTOs quản trị Permissions (`/api/v1/permissions`).
  - Controller & DTOs gán/gỡ Role cho User (`/api/v1/users/{userId}/roles`).
- **O2:** Xác nhận cơ chế phân quyền admin (role nào được gọi REST admin) — phụ thuộc Spring Security integration.
- **O3:** Xác nhận `AuthenticatedPrincipal` được build ở đâu (module authentication hay authorization).

---

## 10. Completed Walkthrough Log

> Cập nhật sau khi triển khai `authorization.api` port và tích hợp token claims.

1. **[DONE]** Tạo package `c4f.vannang.vaops.modules.authorization.api` (`RoleDto`, `PermissionDto`, `AuthorizationAPIService`, `AuthorizationApiMapper`, `PermissionUtils`).
2. **[DONE]** Đổi tên `PermissionQueryRepository.findActiveByUserId` -> `findAllActiveByUserId` cho đồng nhất với `RoleQueryRepository`.
3. **[DONE]** Triển khai `AuthorizationAPIServiceImpl` trong `authorization.internal`.
4. **[DONE]** Cập nhật `AccessTokenClaims` và `JwtAccessTokenProvider` hỗ trợ claims `roles` và `permissions`.
5. **[DONE]** Tích hợp `AuthorizationAPIService` vào `AuthenticationServiceImpl` cho `login` và `refreshToken`.