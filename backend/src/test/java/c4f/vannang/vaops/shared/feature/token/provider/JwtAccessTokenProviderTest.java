package c4f.vannang.vaops.shared.feature.token.provider;

import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.shared.feature.token.claims.AccessTokenClaims;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtAccessTokenProviderTest {

  private JwtAccessTokenProvider provider;

  @BeforeEach
  void setUp() {
    AuthProperties properties = new AuthProperties();
    AuthProperties.Jwt jwt = new AuthProperties.Jwt();
    jwt.setAccessSecret("1234567890123456789012345678901234567890"); // 40+ chars secret
    jwt.setAccessExpirationMs(3600000L);
    jwt.setIssuer("vaops-test");
    properties.setJwt(jwt);

    provider = new JwtAccessTokenProvider(properties);
  }

  @Test
  @DisplayName("Should generate token with roles and permissions and parse them back")
  void testGenerateAndValidateWithRolesAndPermissions() {
    UUID userId = UUID.randomUUID();
    List<String> roles = List.of("ADMIN", "USER");
    List<String> permissions = List.of("USER:READ", "USER:WRITE");
    AccessTokenClaims claims = new AccessTokenClaims(userId, "john.doe", roles, permissions);

    String token = provider.generate(claims);
    assertNotNull(token);

    AccessTokenClaims validatedClaims = provider.validate(token);
    assertEquals(userId, validatedClaims.userId());
    assertEquals("john.doe", validatedClaims.accountName());
    assertEquals(roles, validatedClaims.roles());
    assertEquals(permissions, validatedClaims.permissions());
  }
}