package c4f.vannang.vaops.modules.authorization.internal.domain.converter;

import c4f.vannang.vaops.modules.authorization.internal.domain.valueobject.RoleCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleCodeConverter implements AttributeConverter<RoleCode, String> {
  @Override
  public String convertToDatabaseColumn(RoleCode attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public RoleCode convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new RoleCode(dbData);
  }
}
