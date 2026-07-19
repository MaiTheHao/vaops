package c4f.vannang.vaops.modules.identity.internal.domain.converter;

import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
class AccountNameConverter implements AttributeConverter<AccountName, String> {

  @Override
  public String convertToDatabaseColumn(AccountName attribute) {
    return attribute == null ? null : attribute.value();
  }

  @Override
  public AccountName convertToEntityAttribute(String dbData) {
    return dbData == null ? null : new AccountName(dbData);
  }
}
