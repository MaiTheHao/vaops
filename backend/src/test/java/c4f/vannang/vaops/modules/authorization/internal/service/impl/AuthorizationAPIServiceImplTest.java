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