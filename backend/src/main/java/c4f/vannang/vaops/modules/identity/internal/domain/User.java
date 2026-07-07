package c4f.vannang.vaops.modules.identity.internal.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "account_name", nullable = false, unique = true, length = 256)
  private String accountName;

  @Column(name = "password_hash", nullable = false, length = 256)
  private String passwordHash;

  @Column(name = "display_name", nullable = true, length = 256)
  private String displayName;

  @Column(name = "avatar_url", nullable = true, length = 1024)
  private String avatarUrl;

  @Column(name = "failed_login_count", nullable = false)
  private int failedLoginCount = 0;

  @Column(name = "locked_until", nullable = true)
  private Instant lockedUntil;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "last_login_at", nullable = true)
  private Instant lastLoginAt;

  @Column(name = "deleted_at", nullable = true)
  private Instant deletedAt;

  @Column(name = "deleted_by", nullable = true)
  private UUID deletedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public static User register(
      String accountName,
      String passwordHash,
      String displayName,
      String avatarUrl) {

    User user = new User();
    user.accountName = accountName;
    user.passwordHash = passwordHash;
    user.displayName = displayName;
    user.avatarUrl = avatarUrl;
    user.failedLoginCount = 0;
    user.active = true;

    return user;
  }

  public void recordSuccessfulLogin() {
    this.failedLoginCount = 0;
    this.lastLoginAt = Instant.now();
    this.lockedUntil = null;
  }

  public void recordFailedLogin(int maxAttempts, Duration lockDuration) {
    this.failedLoginCount++;
    if (this.failedLoginCount >= maxAttempts) {
      this.lockedUntil = Instant.now().plus(lockDuration);
    }
  }

  public boolean isLocked() {
    return lockedUntil != null && Instant.now().isBefore(lockedUntil);
  }

  public void updateProfile(String displayName, String avatarUrl) {
    this.displayName = displayName;
    this.avatarUrl = avatarUrl;
  }

  public void changePassword(String newPasswordHash) {
    this.passwordHash = newPasswordHash;
  }

  public void softDelete(UUID deletedByUserId) {
    this.deletedAt = Instant.now();
    this.deletedBy = deletedByUserId;
    this.active = false;
  }

  public void activate() {
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}
