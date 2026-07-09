package c4f.vannang.vaops.modules.identity.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record PasswordHash(String value) {

  public PasswordHash {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Password hash must not be null or blank");
    }
  }
}