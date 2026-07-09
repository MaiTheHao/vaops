package c4f.vannang.vaops.modules.identity.internal.domain.converter;

import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PasswordHashConverter implements AttributeConverter<PasswordHash, String> {

  @Override
  public String convertToDatabaseColumn(PasswordHash attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PasswordHash convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PasswordHash(dbData);
  }
}
