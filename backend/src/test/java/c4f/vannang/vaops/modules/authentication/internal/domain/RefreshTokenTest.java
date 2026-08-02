package c4f.vannang.vaops.modules.authentication.internal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private final UUID userId = UUID.randomUUID();
  private final String tokenHash = "hashed-token-value";
  private final Instant futureExpiry = Instant.now().plus(1, ChronoUnit.HOURS);
  private final Instant pastExpiry = Instant.now().minus(1, ChronoUnit.HOURS);

  private RefreshToken createValidToken() {
    return RefreshToken.create(userId, tokenHash, futureExpiry);
  }

  @Test
  void create_ShouldSetCorrectFields_WhenInvoked() {
    // given
    // when
    RefreshToken token = RefreshToken.create(userId, tokenHash, futureExpiry);

    // then
    assertThat(token.getUserId()).isEqualTo(userId);
    assertThat(token.getTokenHash()).isEqualTo(tokenHash);
    assertThat(token.getExpiredAt()).isEqualTo(futureExpiry);
    assertThat(token.getRevokedAt()).isNull();
    assertThat(token.getCreatedAt()).isNotNull();
  }

  @Test
  void revoke_ShouldSetRevokedAt_WhenTokenIsNotRevoked() {
    // given
    RefreshToken token = createValidToken();
    assertThat(token.getRevokedAt()).isNull();

    // when
    token.revoke();

    // then
    assertThat(token.getRevokedAt()).isNotNull();
  }

  @Test
  void revoke_ShouldBeIdempotent_WhenInvokedTwice() {
    // given
    RefreshToken token = createValidToken();

    // when
    token.revoke();
    Instant firstRevokedAt = token.getRevokedAt();
    token.revoke();

    // then
    assertThat(token.getRevokedAt()).isEqualTo(firstRevokedAt);
  }

  @Test
  void isExpired_ShouldReturnTrue_WhenExpiredAtIsInThePast() {
    // given
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);

    // when
    boolean expired = token.isExpired();

    // then
    assertThat(expired).isTrue();
  }

  @Test
  void isExpired_ShouldReturnFalse_WhenExpiredAtIsInTheFuture() {
    // given
    RefreshToken token = createValidToken();

    // when
    boolean expired = token.isExpired();

    // then
    assertThat(expired).isFalse();
  }

  @Test
  void isRevoked_ShouldReturnTrue_WhenTokenHasBeenRevoked() {
    // given
    RefreshToken token = createValidToken();
    token.revoke();

    // when
    boolean revoked = token.isRevoked();

    // then
    assertThat(revoked).isTrue();
  }

  @Test
  void isRevoked_ShouldReturnFalse_WhenTokenHasNotBeenRevoked() {
    // given
    RefreshToken token = createValidToken();

    // when
    boolean revoked = token.isRevoked();

    // then
    assertThat(revoked).isFalse();
  }

  @Test
  void isValid_ShouldReturnTrue_WhenNotExpiredAndNotRevoked() {
    // given
    RefreshToken token = createValidToken();

    // when
    boolean valid = token.isValid();

    // then
    assertThat(valid).isTrue();
  }

  @Test
  void isValid_ShouldReturnFalse_WhenExpired() {
    // given
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);

    // when
    boolean valid = token.isValid();

    // then
    assertThat(valid).isFalse();
  }

  @Test
  void isValid_ShouldReturnFalse_WhenRevoked() {
    // given
    RefreshToken token = createValidToken();
    token.revoke();

    // when
    boolean valid = token.isValid();

    // then
    assertThat(valid).isFalse();
  }

  @Test
  void isValid_ShouldReturnFalse_WhenExpiredAndRevoked() {
    // given
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);
    token.revoke();

    // when
    boolean valid = token.isValid();

    // then
    assertThat(valid).isFalse();
  }

  @Test
  void setId_ShouldSetId_WhenInvoked() {
    // given
    RefreshToken token = createValidToken();
    UUID expectedId = UUID.randomUUID();

    // when
    token.setId(expectedId);

    // then
    assertThat(token.getId()).isEqualTo(expectedId);
  }
}