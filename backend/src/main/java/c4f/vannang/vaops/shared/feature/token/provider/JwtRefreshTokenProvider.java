package c4f.vannang.vaops.shared.feature.token.provider;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.feature.token.RefreshTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.RefreshTokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public final class JwtRefreshTokenProvider implements RefreshTokenSpec {

  private final SecretKey refreshKey;
  private final AuthProperties authProperties;

  public JwtRefreshTokenProvider(AuthProperties authProperties) {
    this.authProperties = authProperties;
    this.refreshKey = Keys.hmacShaKeyFor(
        authProperties.getJwt().getRefreshSecret().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generate(RefreshTokenClaims claims) {
    Instant now = Instant.now();
    Instant expiry = now.plusMillis(authProperties.getJwt().getRefreshExpirationMs());

    return Jwts.builder()
        .issuer(authProperties.getJwt().getIssuer())
        .subject(claims.userId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(refreshKey)
        .compact();
  }

  @Override
  public RefreshTokenClaims validate(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(refreshKey).build().parseSignedClaims(token).getPayload();

      return new RefreshTokenClaims(UUID.fromString(claims.getSubject()));
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Refresh token expired");
    } catch (Exception e) {
      throw new UnauthenticatedException("Invalid refresh token");
    }
  }
}
