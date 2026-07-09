package c4f.vannang.vaops.modules.identity.internal.domain.converter;

import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AvatarUrlConverter implements AttributeConverter<AvatarUrl, String> {

  @Override
  public String convertToDatabaseColumn(AvatarUrl attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public AvatarUrl convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new AvatarUrl(dbData);
  }
}
