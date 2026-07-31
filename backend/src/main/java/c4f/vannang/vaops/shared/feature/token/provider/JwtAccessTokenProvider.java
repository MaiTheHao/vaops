package c4f.vannang.vaops.shared.feature.token.provider;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.feature.token.AccessTokenSpec;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
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
public final class JwtAccessTokenProvider implements AccessTokenSpec {

  private final SecretKey accessKey;
  private final AuthProperties authProperties;

  public JwtAccessTokenProvider(AuthProperties authProperties) {
    this.authProperties = authProperties;
    this.accessKey = Keys.hmacShaKeyFor(
        authProperties.getJwt().getAccessSecret().getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generate(AccessTokenClaims claims) {
    Instant now = Instant.now();
    Instant expiry = now.plusMillis(authProperties.getJwt().getAccessExpirationMs());

    return Jwts.builder()
        .issuer(authProperties.getJwt().getIssuer())
        .subject(claims.accountName())
        .claim("userId", claims.userId().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(accessKey)
        .compact();
  }

  @Override
  public AccessTokenClaims validate(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();

      String userIdStr = claims.get("userId", String.class);
      if (userIdStr == null) {
        throw new UnauthenticatedException("Invalid token claims");
      }
      return new AccessTokenClaims(UUID.fromString(userIdStr), claims.getSubject());
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Access token expired");
    } catch (Exception e) {
      throw new UnauthenticatedException("Invalid token");
    }
  }
}
