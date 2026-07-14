package c4f.vannang.vaops.modules.authentication.internal.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor
public class RefreshToken {

  @Id
  @UuidGenerator
  @Column(name = "id")
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, length = 256)
  private String tokenHash;

  @Column(name = "expired_at", nullable = false)
  private Instant expiredAt;

  @Column(name = "revoked_at", nullable = true)
  private Instant revokedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public static RefreshToken create(UUID userId, String tokenHash, Instant expiredAt) {
    RefreshToken token = new RefreshToken();
    token.userId = userId;
    token.tokenHash = tokenHash;
    token.expiredAt = expiredAt;
    return token;
  }

  public void revoke() {
    if (this.revokedAt == null) {
      this.revokedAt = Instant.now();
    }
  }

  public boolean isExpired() {
    return Instant.now().isAfter(this.expiredAt);
  }

  public boolean isRevoked() {
    return this.revokedAt != null;
  }

  public boolean isValid() {
    return !isExpired() && !isRevoked();
  }

  public void setId(UUID id) {
    this.id = id;
  }
}
