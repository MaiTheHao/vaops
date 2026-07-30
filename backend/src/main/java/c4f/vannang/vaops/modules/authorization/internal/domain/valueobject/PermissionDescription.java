package c4f.vannang.vaops.modules.authorization.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PermissionDescription(String value) {

  public PermissionDescription {
    if (value != null) {
      value = value.strip();
      if (value.length() > 1024) {
        throw new ValidationException("Permission description must not exceed 1024 characters");
      }
    }
  }
}
