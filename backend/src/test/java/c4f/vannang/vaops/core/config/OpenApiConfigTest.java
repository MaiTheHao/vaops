package c4f.vannang.vaops.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import c4f.vannang.vaops.core.constant.AuthConstant;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

  private OpenApiConfig openApiConfig;

  @BeforeEach
  void setUp() {
    openApiConfig = new OpenApiConfig();
  }

  @Test
  @DisplayName("customOpenApi should configure cookie security schemes and default security requirement")
  void customOpenApi_ShouldConfigureSecuritySchemesAndRequirement() {
    // when
    OpenAPI openAPI = openApiConfig.customOpenApi();

    // then
    assertThat(openAPI).isNotNull();
    assertThat(openAPI.getComponents()).isNotNull();

    // Verify Access Token Security Scheme
    SecurityScheme accessScheme = openAPI.getComponents().getSecuritySchemes().get(AuthConstant.ACCESS_TOKEN_KEY);
    assertThat(accessScheme).isNotNull();
    assertThat(accessScheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
    assertThat(accessScheme.getIn()).isEqualTo(SecurityScheme.In.COOKIE);
    assertThat(accessScheme.getName()).isEqualTo(AuthConstant.ACCESS_TOKEN_KEY);

    // Verify Refresh Token Security Scheme
    SecurityScheme refreshScheme = openAPI.getComponents().getSecuritySchemes().get(AuthConstant.REFRESH_TOKEN_KEY);
    assertThat(refreshScheme).isNotNull();
    assertThat(refreshScheme.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
    assertThat(refreshScheme.getIn()).isEqualTo(SecurityScheme.In.COOKIE);
    assertThat(refreshScheme.getName()).isEqualTo(AuthConstant.REFRESH_TOKEN_KEY);

    // Verify Default Security Item
    assertThat(openAPI.getSecurity()).isNotEmpty();
    SecurityRequirement securityRequirement = openAPI.getSecurity().get(0);
    assertThat(securityRequirement).containsKey(AuthConstant.ACCESS_TOKEN_KEY);
  }
}
