package c4f.vannang.vaops.modules.identity.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record DisplayName(String value) {

  public DisplayName {
    if (value != null) {
      value = value.strip();
      if (value.isEmpty()) {
        throw new ValidationException("Display name must not be empty if provided");
      }
      if (value.length() > 256) {
        throw new ValidationException("Display name must not exceed 256 characters");
      }
    }
  }
}