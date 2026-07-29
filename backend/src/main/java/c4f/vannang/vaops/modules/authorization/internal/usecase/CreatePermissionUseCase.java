package c4f.vannang.vaops.modules.authorization.internal.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import c4f.vannang.vaops.modules.authorization.internal.domain.Permission;
import c4f.vannang.vaops.modules.authorization.internal.dto.CreatePermissionCommand;
import c4f.vannang.vaops.modules.authorization.internal.dto.PermissionResponse;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionQueryRepository;
import c4f.vannang.vaops.modules.authorization.internal.repository.PermissionWriteRepository;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatePermissionUseCase {

  private final PermissionQueryRepository permissionQueryRepository;
  private final PermissionWriteRepository permissionWriteRepository;

  public PermissionResponse execute(CreatePermissionCommand command) {
    if (command == null || command.resource() == null || command.resource().isBlank()
        || command.action() == null || command.action().isBlank()) {
      throw new ValidationException("Permission resource and action must not be empty");
    }

    String resource = command.resource().trim();
    String action = command.action().trim();

    if (permissionQueryRepository.findByResourceAndAction(resource, action).isPresent()) {
      throw new ResourceAlreadyExistsException("Permission with resource '" + resource + "' and action '" + action + "' already exists");
    }

    Permission permission = Permission.builder()
        .resource(resource)
        .action(action)
        .description(command.description())
        .createdBy(command.createdBy())
        .isActive(true)
        .build();

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
