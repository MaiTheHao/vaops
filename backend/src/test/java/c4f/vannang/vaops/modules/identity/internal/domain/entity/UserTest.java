package c4f.vannang.vaops.modules.identity.internal.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import c4f.vannang.vaops.modules.identity.internal.domain.User;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AccountName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.AvatarUrl;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.DisplayName;
import c4f.vannang.vaops.modules.identity.internal.domain.valueobject.PasswordHash;
import c4f.vannang.vaops.shared.exception.ValidationException;

class UserTest {

  private final AccountName accountName = new AccountName("testuser");
  private final PasswordHash passwordHash = new PasswordHash("hashed-password");
  private final DisplayName displayName = new DisplayName("Test User");
  private final AvatarUrl avatarUrl = new AvatarUrl("https://example.com/avatar.png");

  private User reconstituteUser() {
    return User.register(accountName, passwordHash, displayName, avatarUrl);
  }

  @Test
  void register_shouldCreateUserWithCorrectFields() {
    User user = User.register(accountName, passwordHash, displayName, avatarUrl);

    assertEquals("testuser", user.getAccountName().value());
    assertEquals("hashed-password", user.getPasswordHash().value());
    assertEquals("Test User", user.getDisplayName().value());
    assertEquals("https://example.com/avatar.png", user.getAvatarUrl().value());
    assertEquals(0, user.getFailedLoginCount());
    assertTrue(user.isActive());
  }

  @Test
  void recordSuccessfulLogin_shouldResetFailedLoginCountAndSetLastLoginAtAndClearLockedUntil() {
    User user = reconstituteUser();
    user.recordSuccessfulLogin();

    assertEquals(0, user.getFailedLoginCount());
    assertNotNull(user.getLastLoginAt());
    assertNull(user.getLockedUntil());
  }

  @Test
  void recordFailedLogin_shouldIncrementCounter() {
    User user = reconstituteUser();
    assertEquals(0, user.getFailedLoginCount());

    user.recordFailedLogin(5, Duration.ofMinutes(15));
    assertEquals(1, user.getFailedLoginCount());
  }

  @Test
  void recordFailedLogin_shouldLockAccountWhenReachesMaxAttempts() {
    User user = reconstituteUser();

    for (int i = 0; i < 4; i++) {
      user.recordFailedLogin(5, Duration.ofMinutes(15));
    }
    assertNull(user.getLockedUntil());

    user.recordFailedLogin(5, Duration.ofMinutes(15));
    assertEquals(5, user.getFailedLoginCount());
    assertNotNull(user.getLockedUntil());
  }

  @Test
  void isLocked_shouldReturnTrueWhenLockedUntilInFuture() {
    User user = reconstituteUser();
    user.recordFailedLogin(1, Duration.ofMinutes(15));
    assertTrue(user.isLocked());
  }

  @Test
  void isLocked_shouldReturnFalseWhenLockedUntilIsNull() {
    User user = reconstituteUser();
    assertFalse(user.isLocked());
  }

  @Test
  void updateProfile_shouldUpdateDisplayNameAndAvatarUrl() {
    User user = reconstituteUser();
    user.updateProfile(new DisplayName("New Name"), new AvatarUrl("https://example.com/new-avatar.png"));

    assertEquals("New Name", user.getDisplayName().value());
    assertEquals("https://example.com/new-avatar.png", user.getAvatarUrl().value());
  }

  @Test
  void changePassword_shouldUpdatePasswordHash() {
    User user = reconstituteUser();
    PasswordHash newHash = new PasswordHash("new-hashed-password");
    user.changePassword(newHash);

    assertEquals("new-hashed-password", user.getPasswordHash().value());
  }

  @Test
  void softDelete_shouldSetDeletedAtDeletedByAndSetActiveFalse() {
    User user = reconstituteUser();
    UUID deletedBy = UUID.randomUUID();
    user.softDelete(deletedBy);

    assertNotNull(user.getDeletedAt());
    assertEquals(deletedBy, user.getDeletedBy());
    assertFalse(user.isActive());
  }

  @Test
  void activate_shouldSetActiveTrue() {
    User user = reconstituteUser();
    user.deactivate();
    assertFalse(user.isActive());

    user.activate();
    assertTrue(user.isActive());
  }

  @Test
  void deactivate_shouldSetActiveFalse() {
    User user = reconstituteUser();
    assertTrue(user.isActive());

    user.deactivate();
    assertFalse(user.isActive());
  }

  // --- Value Object validation tests ---

  @Test
  void accountName_shouldRejectNull() {
    assertThrows(ValidationException.class, () -> new AccountName(null));
  }

  @Test
  void accountName_shouldRejectBlank() {
    assertThrows(ValidationException.class, () -> new AccountName("  "));
  }

  @Test
  void accountName_shouldRejectTooLong() {
    assertThrows(ValidationException.class, () -> new AccountName("a".repeat(257)));
  }

  @Test
  void accountName_shouldTrim() {
    AccountName name = new AccountName("  testuser  ");
    assertEquals("testuser", name.value());
  }

  @Test
  void passwordHash_shouldRejectNull() {
    assertThrows(ValidationException.class, () -> new PasswordHash(null));
  }

  @Test
  void passwordHash_shouldRejectBlank() {
    assertThrows(ValidationException.class, () -> new PasswordHash("  "));
  }

  @Test
  void displayName_shouldAllowNull() {
    DisplayName dn = new DisplayName(null);
    assertNull(dn.value());
  }

  @Test
  void displayName_shouldRejectEmpty() {
    assertThrows(ValidationException.class, () -> new DisplayName(""));
  }

  @Test
  void displayName_shouldRejectTooLong() {
    assertThrows(ValidationException.class, () -> new DisplayName("a".repeat(257)));
  }

  @Test
  void avatarUrl_shouldAllowNull() {
    AvatarUrl au = new AvatarUrl(null);
    assertNull(au.value());
  }

  @Test
  void avatarUrl_shouldRejectEmpty() {
    assertThrows(ValidationException.class, () -> new AvatarUrl(""));
  }

  @Test
  void avatarUrl_shouldRejectTooLong() {
    assertThrows(ValidationException.class, () -> new AvatarUrl("a".repeat(1025)));
  }

  @Test
  void validatePasswordStrength_shouldRejectNull() {
    assertThrows(ValidationException.class, () -> User.validatePasswordStrength(null));
  }

  @Test
  void validatePasswordStrength_shouldRejectShortPassword() {
    assertThrows(ValidationException.class, () -> User.validatePasswordStrength("short"));
  }

  @Test
  void validatePasswordStrength_shouldAcceptValidPassword() {
    assertDoesNotThrow(() -> User.validatePasswordStrength("password123"));
  }
}