package c4f.vannang.vaops.modules.authorization.internal.service.impl;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionSearchCriteria;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.spec.PermissionSpecification;
import c4f.vannang.vaops.modules.authorization.internal.service.PermissionService;
import c4f.vannang.vaops.shared.dto.PageResponse;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements PermissionService {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;

  @Override
  public Permission createPermission(CreatePermissionCommand command) {
    if (command == null) {
      throw new ValidationException("Command must not be null");
    }
    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description = new PermissionDescription(command.description());

    if (permissionQueryRepository.existsByResourceAndAction(resource, action)) {
      throw new ResourceAlreadyExistsException(
          "Permission with resource and action already exists");
    }

    Permission permission = Permission.create(resource, action, description);
    return permissionWriteRepository.save(permission);
  }

  @Override
  public Permission updatePermission(UpdatePermissionCommand command) {
    if (command == null || command.id() == null) {
      throw new ValidationException("Command and ID must not be null");
    }
    Permission permission = permissionQueryRepository
        .findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description = new PermissionDescription(command.description());

    permission.update(resource, action, description);
    return permissionWriteRepository.save(permission);
  }

  @Override
  public void softDeletePermission(UUID id, UUID deletedBy) {
    if (id == null) throw new ValidationException("ID must not be null");
    Permission permission = permissionQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

    permission.softDelete(deletedBy);
    permissionWriteRepository.save(permission);
  }

  @Override
  public void hardDeletePermission(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!permissionQueryRepository.existsByIdWithDeleted(id)) {
      throw new ResourceNotFoundException("Permission not found");
    }
    permissionWriteRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Permission getPermissionById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    return permissionQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<Permission> searchPermissions(PermissionSearchCriteria criteria) {
    Page<Permission> permissionPage = permissionQueryRepository.findAll(
        PermissionSpecification.search(criteria),
        criteria != null ? criteria.toPageable() : Pageable.unpaged());
    return PageResponse.from(permissionPage, permission -> permission);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasPermission(UUID userId, String resource, String action) {
    if (userId == null || resource == null || action == null) {
      throw new ValidationException("UserId, resource and action must not be null");
    }
    PermissionResource permResource = new PermissionResource(resource);
    PermissionAction permAction = new PermissionAction(action);
    return permissionQueryRepository.existsActiveByUserIdAndResourceAndAction(userId, permResource, permAction);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Permission> getPermissionsByUserId(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    return permissionQueryRepository.findActiveByUserId(userId);
  }
}
