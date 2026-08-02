package c4f.vannang.vaops.modules.identity.internal.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

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
    // given
    User user = User.register(accountName, passwordHash, displayName, avatarUrl);

    // when
    // (registration happens in given)

    // then
    assertThat(user.getAccountName().value()).isEqualTo("testuser");
    assertThat(user.getPasswordHash().value()).isEqualTo("hashed-password");
    assertThat(user.getDisplayName().value()).isEqualTo("Test User");
    assertThat(user.getAvatarUrl().value()).isEqualTo("https://example.com/avatar.png");
    assertThat(user.getFailedLoginCount()).isZero();
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void recordSuccessfulLogin_shouldResetFailedLoginCountAndSetLastLoginAtAndClearLockedUntil() {
    // given
    User user = reconstituteUser();
    user.recordFailedLogin();
    user.recordFailedLogin();

    // when
    user.recordSuccessfulLogin();

    // then
    assertThat(user.getFailedLoginCount()).isZero();
    assertThat(user.getLastLoginAt()).isNotNull();
    assertThat(user.getLockedUntil()).isNull();
  }

  @Test
  void recordFailedLogin_shouldIncrementCounter() {
    // given
    User user = reconstituteUser();
    assertThat(user.getFailedLoginCount()).isZero();

    // when
    user.recordFailedLogin();

    // then
    assertThat(user.getFailedLoginCount()).isEqualTo(1);
  }

  @Test
  void recordFailedLogin_shouldLockAccountWhenReachesMaxAttempts() {
    // given
    User user = reconstituteUser();

    // when
    for (int i = 0; i < 4; i++) {
      user.recordFailedLogin();
    }

    // then
    assertThat(user.getLockedUntil()).isNull();

    // when
    user.recordFailedLogin();

    // then
    assertThat(user.getFailedLoginCount()).isEqualTo(5);
    assertThat(user.getLockedUntil()).isNotNull();
  }

  @Test
  void isLocked_shouldReturnTrueWhenLockedUntilInFuture() {
    // given
    User user = reconstituteUser();
    for (int i = 0; i < 5; i++) {
      user.recordFailedLogin();
    }

    // when
    boolean locked = user.isLocked();

    // then
    assertThat(locked).isTrue();
  }

  @Test
  void isLocked_shouldReturnFalseWhenLockedUntilIsNull() {
    // given
    User user = reconstituteUser();

    // when
    boolean locked = user.isLocked();

    // then
    assertThat(locked).isFalse();
  }

  @Test
  void updateProfile_shouldUpdateDisplayNameAndAvatarUrl() {
    // given
    User user = reconstituteUser();

    // when
    user.updateProfile(new DisplayName("New Name"), new AvatarUrl("https://example.com/new-avatar.png"));

    // then
    assertThat(user.getDisplayName().value()).isEqualTo("New Name");
    assertThat(user.getAvatarUrl().value()).isEqualTo("https://example.com/new-avatar.png");
  }

  @Test
  void changePassword_shouldUpdatePasswordHash() {
    // given
    User user = reconstituteUser();
    PasswordHash newHash = new PasswordHash("new-hashed-password");

    // when
    user.changePassword(newHash);

    // then
    assertThat(user.getPasswordHash().value()).isEqualTo("new-hashed-password");
  }

  @Test
  void softDelete_shouldSetDeletedAtAndDeletedBy() {
    // given
    User user = reconstituteUser();
    UUID deletedBy = UUID.randomUUID();

    // when
    user.softDelete(deletedBy);

    // then
    assertThat(user.isDeleted()).isTrue();
    assertThat(user.getDeletedAt()).isNotNull();
    assertThat(user.getDeletedBy()).isEqualTo(deletedBy);
  }

  @Test
  void activate_shouldSetActiveTrue() {
    // given
    User user = reconstituteUser();
    user.deactivate();
    assertThat(user.isActive()).isFalse();

    // when
    user.activate();

    // then
    assertThat(user.isActive()).isTrue();
  }

  @Test
  void deactivate_shouldSetActiveFalse() {
    // given
    User user = reconstituteUser();
    assertThat(user.isActive()).isTrue();

    // when
    user.deactivate();

    // then
    assertThat(user.isActive()).isFalse();
  }

  // --- Value Object validation tests ---

  @Test
  void accountName_shouldRejectNull() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new AccountName(null))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Account name must not be null or blank");
  }

  @Test
  void accountName_shouldRejectBlank() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new AccountName("  "))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Account name must not be null or blank");
  }

  @Test
  void accountName_shouldRejectTooLong() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new AccountName("a".repeat(257)))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Account name must not exceed 256 characters");
  }

  @Test
  void accountName_shouldTrim() {
    // given
    // (no setup required)

    // when
    AccountName name = new AccountName("  testuser  ");

    // then
    assertThat(name.value()).isEqualTo("testuser");
  }

  @Test
  void passwordHash_shouldRejectNull() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new PasswordHash(null))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Password hash must not be null or blank");
  }

  @Test
  void passwordHash_shouldRejectBlank() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new PasswordHash("  "))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Password hash must not be null or blank");
  }

  @Test
  void displayName_shouldAllowNull() {
    // given
    // (no setup required)

    // when
    DisplayName dn = new DisplayName(null);

    // then
    assertThat(dn.value()).isNull();
  }

  @Test
  void displayName_shouldRejectEmpty() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new DisplayName(""))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Display name must not be empty if provided");
  }

  @Test
  void displayName_shouldRejectTooLong() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new DisplayName("a".repeat(257)))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Display name must not exceed 256 characters");
  }

  @Test
  void avatarUrl_shouldAllowNull() {
    // given
    // (no setup required)

    // when
    AvatarUrl au = new AvatarUrl(null);

    // then
    assertThat(au.value()).isNull();
  }

  @Test
  void avatarUrl_shouldRejectEmpty() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new AvatarUrl(""))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Avatar URL must not be empty if provided");
  }

  @Test
  void avatarUrl_shouldRejectTooLong() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> new AvatarUrl("a".repeat(1025)))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Avatar URL must not exceed 1024 characters");
  }

  @Test
  void validatePasswordStrength_shouldRejectNull() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> User.validatePasswordStrength(null))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Password must be at least 8 characters long");
  }

  @Test
  void validatePasswordStrength_shouldRejectShortPassword() {
    // given
    // (no setup required)

    // when / then
    assertThatThrownBy(() -> User.validatePasswordStrength("short"))
        .isInstanceOf(ValidationException.class)
        .hasMessage("Password must be at least 8 characters long");
  }

  @Test
  void validatePasswordStrength_shouldAcceptValidPassword() {
    // given
    // (no setup required)

    // when
    User.validatePasswordStrength("password123");

    // then
    // (no exception thrown)
  }
}