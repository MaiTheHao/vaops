package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionDescription;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionDescriptionConverter implements AttributeConverter<PermissionDescription, String> {
  @Override
  public String convertToDatabaseColumn(PermissionDescription attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionDescription convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionDescription(dbData);
  }
}
