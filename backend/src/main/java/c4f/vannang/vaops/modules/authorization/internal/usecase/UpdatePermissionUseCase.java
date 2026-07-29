package c4f.vannang.vaops.modules.authorization.internal.usecase;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.dto.UpdatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ResourceNotFoundException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdatePermissionUseCase {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;

  public PermissionResponse execute(UpdatePermissionCommand command) {
    if (command == null || command.id() == null || command.resource() == null || command.resource().isBlank()
        || command.action() == null || command.action().isBlank()) {
      throw new ValidationException("Permission id, resource, and action must not be empty");
    }

    Permission permission = permissionQueryRepository
        .findActiveById(command.id())
        .orElseThrow(() -> new ResourceNotFoundException("Permission not found or is inactive: " + command.id()));

    String newResource = command.resource().trim();
    String newAction = command.action().trim();

    if (!permission.getResource().equalsIgnoreCase(newResource) || !permission.getAction().equalsIgnoreCase(newAction)) {
      Optional<Permission> existing = permissionQueryRepository.findByResourceAndAction(newResource, newAction);
      if (existing.isPresent() && !existing.get().getId().equals(permission.getId())) {
        throw new ResourceAlreadyExistsException("Permission with resource '" + newResource + "' and action '" + newAction + "' already exists");
      }
      permission.setResource(newResource);
      permission.setAction(newAction);
    }

    permission.setDescription(command.description());
    permission.setUpdatedBy(command.updatedBy());

    Permission saved = permissionWriteRepository.save(permission);
    return mapToPermissionResponse(saved);
  }

  private PermissionResponse mapToPermissionResponse(Permission p) {
    return new PermissionResponse(
        p.getId(),
        p.getResource(),
        p.getAction(),
        p.getDescription(),
        p.getIsActive(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }
}
