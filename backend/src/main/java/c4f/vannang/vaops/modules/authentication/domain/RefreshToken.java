package c4f.vannang.vaops.modules.authentication.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "refresh_tokens")
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, length = 256)
  private String tokenHash;

  @Column(name = "expired_at", nullable = false)
  private Instant expiredAt;

  @Column(name = "revoked_at", nullable = true)
  private Instant revokedAt;

  @Column(name = "created_at", nullable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

}
