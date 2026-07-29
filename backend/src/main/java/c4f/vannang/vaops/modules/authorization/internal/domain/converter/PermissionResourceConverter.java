package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.PermissionResource;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PermissionResourceConverter implements AttributeConverter<PermissionResource, String> {
  @Override
  public String convertToDatabaseColumn(PermissionResource attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public PermissionResource convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new PermissionResource(dbData);
  }
}
