package c4f.vannang.vaops.modules.identity.internal.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import c4f.vannang.vaops.modules.identity.internal.domain.User;

class UserTest {

  private final String accountName = "testuser";
  private final String passwordHash = "hashed-password";
  private final String displayName = "Test User";
  private final String avatarUrl = "https://example.com/avatar.png";

  private User reconstituteUser() {
    return User.register(accountName, passwordHash, displayName, avatarUrl);
  }

  @Test
  void register_shouldCreateUserWithCorrectFields() {
    User user = User.register(accountName, passwordHash, displayName, avatarUrl);

    assertEquals(accountName, user.getAccountName());
    assertEquals(passwordHash, user.getPasswordHash());
    assertEquals(displayName, user.getDisplayName());
    assertEquals(avatarUrl, user.getAvatarUrl());
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
    user.updateProfile("New Name", "https://example.com/new-avatar.png");

    assertEquals("New Name", user.getDisplayName());
    assertEquals("https://example.com/new-avatar.png", user.getAvatarUrl());
  }

  @Test
  void changePassword_shouldUpdatePasswordHash() {
    User user = reconstituteUser();
    String newHash = "new-hashed-password";
    user.changePassword(newHash);

    assertEquals(newHash, user.getPasswordHash());
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
}
