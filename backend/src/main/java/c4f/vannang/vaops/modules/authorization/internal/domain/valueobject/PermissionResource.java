package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PermissionResource(String value) {
  public PermissionResource {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Permission resource must not be null or blank");
    }
    value = value.strip().toUpperCase();
    if (value.length() > 256) {
      throw new ValidationException("Permission resource must not exceed 256 characters");
    }
  }
}
