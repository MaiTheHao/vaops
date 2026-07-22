package c4f.vannang.vaops.shared.token.provider;

import c4f.vannang.vaops.core.config.AuthProperties;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.claims.RefreshTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import c4f.vannang.vaops.shared.token.specification.RefreshTokenSpec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider implements TokenProvider, AccessTokenSpec, RefreshTokenSpec {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final AuthProperties authProperties;

    public JwtTokenProvider(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.accessKey = Keys.hmacShaKeyFor(authProperties.getJwt().getAccessSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(authProperties.getJwt().getRefreshSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return "jwt";
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
    public AccessTokenClaims validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

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
    public RefreshTokenClaims validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new RefreshTokenClaims(UUID.fromString(claims.getSubject()));
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Refresh token expired");
        } catch (Exception e) {
            throw new UnauthenticatedException("Invalid refresh token");
        }
    }
}
