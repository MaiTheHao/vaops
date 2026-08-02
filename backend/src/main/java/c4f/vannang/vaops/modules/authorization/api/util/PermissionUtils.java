package c4f.vannang.vaops.modules.authorization.api.util;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;

public final class PermissionUtils {

  private static final String DELIMITER = ":";

  private PermissionUtils() {}

  public static String format(String resource, String action) {
    if (resource == null || action == null) {
      return "";
    }
    return resource.strip().toUpperCase() + DELIMITER + action.strip().toUpperCase();
  }

  public static String format(PermissionDto permissionDto) {
    if (permissionDto == null) {
      return "";
    }
    return format(permissionDto.resource(), permissionDto.action());
  }

  public static String[] parse(String permissionString) {
    if (permissionString == null || permissionString.isBlank()) {
      return new String[] {"", ""};
    }
    String[] parts = permissionString.split(DELIMITER, 2);
    if (parts.length == 2) {
      return new String[] {parts[0].strip(), parts[1].strip()};
    }
    return new String[] {parts[0].strip(), ""};
  }
}