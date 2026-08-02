package c4f.vannang.vaops.modules.authorization.api.util;

import c4f.vannang.vaops.modules.authorization.api.dto.PermissionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionUtilsTest {

  @Test
  @DisplayName("Should format resource and action into RESOURCE:ACTION string")
  void testFormatString() {
    assertEquals("USER:READ", PermissionUtils.format("user", "read"));
    assertEquals("USER:WRITE", PermissionUtils.format(" USER ", " write "));
    assertEquals("", PermissionUtils.format(null, "read"));
    assertEquals("", PermissionUtils.format("user", null));
    assertEquals("", PermissionUtils.format(null, null));

    // Empty or blank resource / action
    assertEquals(":READ", PermissionUtils.format("", "read"));
    assertEquals(":READ", PermissionUtils.format("   ", "read"));
    assertEquals("USER:", PermissionUtils.format("user", ""));
    assertEquals("USER:", PermissionUtils.format("user", "   "));
    assertEquals(":", PermissionUtils.format("", ""));

    // Special non-alphanumeric characters
    assertEquals("USER-PROFILE:READ_ALL", PermissionUtils.format("user-profile", "read_all"));
    assertEquals("USER@DOMAIN:ACTION#1", PermissionUtils.format("user@domain", "action#1"));
  }

  @Test
  @DisplayName("Should format PermissionDto into RESOURCE:ACTION string")
  void testFormatDto() {
    PermissionDto dto = new PermissionDto("ROLE", "CREATE", "Create role");
    assertEquals("ROLE:CREATE", PermissionUtils.format(dto));
    assertEquals("", PermissionUtils.format(null));

    // Dto with empty/blank resource or action
    PermissionDto emptyResourceDto = new PermissionDto("", "CREATE", "Create role");
    assertEquals(":CREATE", PermissionUtils.format(emptyResourceDto));

    PermissionDto emptyActionDto = new PermissionDto("ROLE", "  ", "Create role");
    assertEquals("ROLE:", PermissionUtils.format(emptyActionDto));
  }

  @Test
  @DisplayName("Should parse RESOURCE:ACTION string into String array")
  void testParse() {
    String[] parts = PermissionUtils.parse("USER:READ");
    assertEquals("USER", parts[0]);
    assertEquals("READ", parts[1]);

    // Null, empty, or blank permission string
    String[] nullParts = PermissionUtils.parse(null);
    assertEquals("", nullParts[0]);
    assertEquals("", nullParts[1]);

    String[] emptyParts = PermissionUtils.parse("");
    assertEquals("", emptyParts[0]);
    assertEquals("", emptyParts[1]);

    String[] blankParts = PermissionUtils.parse("   ");
    assertEquals("", blankParts[0]);
    assertEquals("", blankParts[1]);

    // Missing colon delimiter
    String[] noColonParts = PermissionUtils.parse("USERREAD");
    assertEquals("USERREAD", noColonParts[0]);
    assertEquals("", noColonParts[1]);

    String[] noColonBlankParts = PermissionUtils.parse("  USERREAD  ");
    assertEquals("USERREAD", noColonBlankParts[0]);
    assertEquals("", noColonBlankParts[1]);

    // Empty resource or action in permission string
    String[] emptyResourceParts = PermissionUtils.parse(":READ");
    assertEquals("", emptyResourceParts[0]);
    assertEquals("READ", emptyResourceParts[1]);

    String[] emptyActionParts = PermissionUtils.parse("USER:");
    assertEquals("USER", emptyActionParts[0]);
    assertEquals("", emptyActionParts[1]);

    // Special non-alphanumeric characters
    String[] specialCharParts = PermissionUtils.parse("USER-PROFILE:READ_ALL");
    assertEquals("USER-PROFILE", specialCharParts[0]);
    assertEquals("READ_ALL", specialCharParts[1]);

    String[] complexSpecialCharParts = PermissionUtils.parse("RESOURCE-123@#$:ACTION-456!%^");
    assertEquals("RESOURCE-123@#$", complexSpecialCharParts[0]);
    assertEquals("ACTION-456!%^", complexSpecialCharParts[1]);
  }
}