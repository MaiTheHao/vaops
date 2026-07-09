package c4f.vannang.vaops.modules.identity.internal.domain.valueobject;

import c4f.vannang.vaops.shared.exception.ValidationException;

public record AccountName(String value) {

  public AccountName {
    if (value == null || value.isBlank()) {
      throw new ValidationException("Account name must not be null or blank");
    }
    value = value.strip();
    if (value.length() > 256) {
      throw new ValidationException("Account name must not exceed 256 characters");
    }
  }
}