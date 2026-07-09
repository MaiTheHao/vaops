package c4f.vannang.vaops.modules.identity.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record AvatarUrl(String value) {

  public AvatarUrl {
    if (value != null) {
      value = value.strip();
      if (value.isEmpty()) {
        throw new ValidationException("Avatar URL must not be empty if provided");
      }
      if (value.length() > 1024) {
        throw new ValidationException("Avatar URL must not exceed 1024 characters");
      }
    }
  }
}