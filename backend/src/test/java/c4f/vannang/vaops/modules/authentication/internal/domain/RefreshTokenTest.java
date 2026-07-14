package c4f.vannang.vaops.modules.authentication.internal.domain;

import static org.junit.jupiter.api.Assertions.*;

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
  void create_shouldCreateTokenWithCorrectFields() {
    RefreshToken token = RefreshToken.create(userId, tokenHash, futureExpiry);

    assertEquals(userId, token.getUserId());
    assertEquals(tokenHash, token.getTokenHash());
    assertEquals(futureExpiry, token.getExpiredAt());
    assertNull(token.getRevokedAt());
    assertNull(token.getCreatedAt());
  }

  @Test
  void revoke_shouldSetRevokedAt() {
    RefreshToken token = createValidToken();
    assertNull(token.getRevokedAt());

    token.revoke();

    assertNotNull(token.getRevokedAt());
  }

  @Test
  void revoke_shouldBeIdempotent() {
    RefreshToken token = createValidToken();

    token.revoke();
    Instant firstRevokedAt = token.getRevokedAt();

    token.revoke();
    assertEquals(firstRevokedAt, token.getRevokedAt());
  }

  @Test
  void isExpired_shouldReturnTrueWhenExpired() {
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);

    assertTrue(token.isExpired());
  }

  @Test
  void isExpired_shouldReturnFalseWhenNotExpired() {
    RefreshToken token = createValidToken();

    assertFalse(token.isExpired());
  }

  @Test
  void isRevoked_shouldReturnTrueWhenRevoked() {
    RefreshToken token = createValidToken();
    token.revoke();

    assertTrue(token.isRevoked());
  }

  @Test
  void isRevoked_shouldReturnFalseWhenNotRevoked() {
    RefreshToken token = createValidToken();

    assertFalse(token.isRevoked());
  }

  @Test
  void isValid_shouldReturnTrueWhenNotExpiredAndNotRevoked() {
    RefreshToken token = createValidToken();

    assertTrue(token.isValid());
  }

  @Test
  void isValid_shouldReturnFalseWhenExpired() {
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);

    assertFalse(token.isValid());
  }

  @Test
  void isValid_shouldReturnFalseWhenRevoked() {
    RefreshToken token = createValidToken();
    token.revoke();

    assertFalse(token.isValid());
  }

  @Test
  void isValid_shouldReturnFalseWhenExpiredAndRevoked() {
    RefreshToken token = RefreshToken.create(userId, tokenHash, pastExpiry);
    token.revoke();

    assertFalse(token.isValid());
  }

  @Test
  void setId_shouldSetId() {
    RefreshToken token = createValidToken();
    UUID expectedId = UUID.randomUUID();

    token.setId(expectedId);

    assertEquals(expectedId, token.getId());
  }
}
