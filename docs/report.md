# 📋 Tổng hợp Context cuộc trò chuyện

## 1. Yêu cầu ban đầu
Đánh giá 2 file base domain và ảnh hưởng nếu **move `isActive` lên `BaseEntity`**:
- `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseEntity.java`
- `backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseSoftDeletableEntity.java`

---

## 2. Hiện trạng hệ thống (đã khảo sát kỹ)

### Class hierarchy hiện tại
```
BaseEntity (id UUID @Id, createdAt @CreatedDate, setId, equals/hashCode/toString, @EntityListeners)
└── BaseAuditableEntity (createdBy, updatedAt, updatedBy)
    └── BaseSoftDeletableEntity (deletedAt, deletedBy; softDelete)
        implements Activatable { isActive() }
        ├── User (tự khai báo active)
        ├── Permission (tự khai báo active)
        └── BaseVersionedEntity → Role (tự khai báo active)
```

### Entity cụ thể
| Entity | Kế thừa | Có is_active? | Implement Activatable? |
|---|---|---|---|
| `RefreshToken` | `BaseEntity` trực tiếp | ❌ | ❌ |
| `User`, `Permission` | `BaseSoftDeletableEntity` | ✅ | ✅ |
| `Role` | `BaseVersionedEntity` | ✅ | ✅ |
| `RolePermission`, `UserRole` | Không extends base (join, @EmbeddedId) | N/A | N/A |

---

## 3. Các quyết định thiết kế đã chốt (theo thứ tự)

### 🔹 Quyết định 1: Tách Base cho ID + move `createdAt` lên auditable
- `BaseEntity` chỉ giữ: `id`, `setId()`, `equals/hashCode/toString`
- `BaseAuditableEntity` nhận thêm: `createdAt` + `@EntityListeners(AuditingEntityListener.class)`

### 🔹 Quyết định 2: Loại bỏ field `active` khỏi base class
- **Lý do:** `active` mang tính nghiệp vụ cao, không nên nằm ở base
- Mỗi entity tự khai báo `active` nếu cần (`User`, `Role`, `Permission`)

### 🔹 Quyết định 3: `deletedAt` và `isActive` là 2 khái niệm độc lập
- Không liên quan nhau
- `softDelete()` **chỉ edit delete fields** (deletedAt/deletedBy), **không đụng active**

### 🔹 Quyết định 4: Tạo interface `Activatable`
- Thêm interface `c4f.vannang.vaops.shared.base.domain.Activatable` (`boolean isActive()`, `activate()`, `deactivate()`)
- `User`, `Role`, `Permission` đều implement

### 🔹 Quyết định 5: Chuẩn hóa "find luôn skip deletedAt" & 3 Tier Repositories
> **Nguyên tắc toàn cục:** Mọi query `find*`/`exists*` mặc định skip bản ghi đã soft-delete (`deletedAt IS NULL`). Đã xóa = không tìm ra. Service layer **chỉ cần check `active` qua `Activatable`**, không bao giờ check `deletedAt`.

---

## 4. Thiết kế tầng Query (3 Tiers với JPQL chay)

| Tier | Mục đích | Phương thức & JPQL |
|---|---|---|
| **Tier 1** | Mặc định skip deleted | `findById`, `findByAccountName`, `existsBy*`, `findAllByIdIn` → JPQL explicit `AND e.deletedAt IS NULL` |
| **Tier 2** | Nghiệp vụ active | `findActiveById`, `findAllActiveByIdIn`, `findActiveByUserId` → JPQL explicit `AND e.active = true AND e.deletedAt IS NULL` |
| **Tier 3** | Maintenance (hard-delete purge) | `findByIdWithDeleted`, `existsByIdWithDeleted` → JPQL explicit không filter `deletedAt` |

---

## 5. Kết quả giải quyết 3 Vấn đề (Blocking Questions)

### ✅ Vấn đề 1 — Uniqueness DB
- **Quyết định**: Giữ nguyên ràng buộc Unique Full hiện tại của DB. Soft-delete vẫn giữ nguyên key độc nhất ở tầng DB.

### ✅ Vấn đề 2 — Hard-delete purge
- **Quyết định**: Sử dụng JPQL chay explicit với tên phương thức rõ ràng `existsByIdWithDeleted` / `findByIdWithDeleted` cho các hàm hard-delete trong Service (`RoleService`, `PermissionService`).

### ✅ Vấn đề 3 — Bất nhất assign flow
- **Quyết định**: Thống nhất dùng `findAllActiveByIdIn` (Tier 2) cho cả `assignPermissionsToRole` và `unassignPermissionsFromRole` trong `RoleService`.

---

## 6. Trạng thái triển khai & Kiểm thử (Final Status)

- ✅ **Đã hoàn thành 100% việc refactor mã nguồn**:
  - [Activatable.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/shared/base/domain/Activatable.java) created.
  - [BaseSoftDeletableEntity.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/shared/base/domain/BaseSoftDeletableEntity.java) refactored.
  - [User.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/domain/User.java), [Role.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Role.java), [Permission.java](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/domain/Permission.java) updated.
  - Repositories ([UserQueryRepository](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/identity/internal/repository/UserQueryRepository.java), [RoleQueryRepository](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/RoleQueryRepository.java), [PermissionQueryRepository](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/repository/PermissionQueryRepository.java)) standardized with explicit JPQL `@Query`.
  - Services ([RoleService](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/RoleService.java), [PermissionService](file:///home/maithehao/Workspace/projects/vaops/backend/src/main/java/c4f/vannang/vaops/modules/authorization/internal/service/PermissionService.java)) updated to use Tier 3 queries for hard delete and Tier 2 queries for permission assignment.
  - Spec & Plan docs created and committed under `docs/superpowers/`.
- ✅ **Kết quả kiểm thử Maven**: **118/118 unit & integration tests PASS** (Build Success).