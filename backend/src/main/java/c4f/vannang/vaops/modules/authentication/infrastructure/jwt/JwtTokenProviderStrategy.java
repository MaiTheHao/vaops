package c4f.vannang.vaops.modules.authentication.infrastructure.jwt;

import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.modules.authentication.internal.TokenProviderStrategy;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import c4f.vannang.vaops.modules.authentication.internal.dto.AccessTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenClaims;
import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
class JwtTokenProviderStrategy implements TokenProviderStrategy {

  private final AuthProperties authProperties;
  private final SecretKey accessKey;
  private final SecretKey refreshKey;

  public JwtTokenProviderStrategy(AuthProperties authProperties) {
    this.authProperties = authProperties;
    this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(authProperties.getJwt().getAccessSecret()));
    this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(authProperties.getJwt().getRefreshSecret()));
  }

  @Override
  public TokenType getType() {
    return TokenType.JWT;
  }

  @Override
  public String createAccessToken(AccessTokenClaims claims) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .issuer(authProperties.getJwt().getIssuer())
        .claims(claims.toClaimsMap())
        .issuedAt(new Date(now))
        .expiration(new Date(now + authProperties.getJwt().getAccessExpirationMs()))
        .signWith(accessKey)
        .compact();
  }

  @Override
  public String createRefreshToken(RefreshTokenClaims claims) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .issuer(authProperties.getJwt().getIssuer())
        .claims(claims.toClaimsMap())
        .issuedAt(new Date(now))
        .expiration(new Date(now + authProperties.getJwt().getRefreshExpirationMs()))
        .signWith(refreshKey)
        .compact();
  }

  @Override
  public AccessTokenClaims validateAccessToken(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(accessKey)
          .requireIssuer(authProperties.getJwt().getIssuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return new AccessTokenClaims(
          UUID.fromString(claims.get("userId", String.class)), claims.getSubject());
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Access token has expired");
    } catch (JwtException | IllegalArgumentException e) {
      throw new TokenExpiredException("Invalid access token: " + e.getMessage());
    }
  }

  @Override
  public RefreshTokenClaims validateRefreshToken(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(refreshKey)
          .requireIssuer(authProperties.getJwt().getIssuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
      return new RefreshTokenClaims(UUID.fromString(claims.getSubject()));
    } catch (ExpiredJwtException e) {
      throw new TokenExpiredException("Refresh token has expired");
    } catch (JwtException | IllegalArgumentException e) {
      throw new TokenExpiredException("Invalid refresh token: " + e.getMessage());
    }
  }
}
