package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionAction;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionActionConverter implements AttributeConverter<PermissionAction, String> {
  @Override
  public String convertToDatabaseColumn(PermissionAction attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionAction convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionAction(dbData);
  }
}
