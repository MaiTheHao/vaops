package c4f.vannang.vaops.modules.authorization.internal.service;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.mapper.PermissionResponseMapper;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;
  private final PermissionResponseMapper permissionResponseMapper;

  public PermissionResponse createPermission(CreatePermissionCommand command) {
    if (command == null) {
      throw new ValidationException("Command must not be null");
    }
    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description =
        command.description() != null ? new PermissionDescription(command.description()) : null;

    if (permissionQueryRepository.existsByResourceAndAction(resource.value(), action.value())) {
      throw new ResourceAlreadyExistsException(
          "Permission with resource and action already exists");
    }

    Permission permission = Permission.create(resource, action, description, command.createdBy());
    Permission saved = permissionWriteRepository.save(permission);
    return permissionResponseMapper.toResponse(saved);
  }

  public PermissionResponse updatePermission(UpdatePermissionCommand command) {
    if (command == null || command.id() == null) {
      throw new ValidationException("Command and ID must not be null");
    }
    Permission permission = permissionQueryRepository
        .findById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

    PermissionResource resource = new PermissionResource(command.resource());
    PermissionAction action = new PermissionAction(command.action());
    PermissionDescription description =
        command.description() != null ? new PermissionDescription(command.description()) : null;

    permission.updateInfo(resource, action, description, command.updatedBy());
    Permission saved = permissionWriteRepository.save(permission);
    return permissionResponseMapper.toResponse(saved);
  }

  public void deletePermission(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    if (!permissionQueryRepository.findById(id).isPresent()) {
      throw new ResourceNotFoundException("Permission not found");
    }
    permissionWriteRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public PermissionResponse getPermissionById(UUID id) {
    if (id == null) throw new ValidationException("ID must not be null");
    Permission permission = permissionQueryRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));
    return permissionResponseMapper.toResponse(permission);
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> listPermissions() {
    return permissionQueryRepository.findAllActive().stream()
        .map(permissionResponseMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PermissionResponse> getUserPermissions(UUID userId) {
    if (userId == null) throw new ValidationException("UserId must not be null");
    List<Permission> permissions = permissionQueryRepository.findActivePermissionsByUserId(userId);
    return permissions.stream()
        .map(permissionResponseMapper::toResponse)
        .collect(Collectors.toList());
  }
}
