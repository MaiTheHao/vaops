package c4f.vannang.vaops.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import c4f.vannang.vaops.core.constant.AuthConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
@Profile("!prod")
@ConditionalOnProperty(value = "vaops.openapi.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {
  
  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
      .components(
        new Components().addSecuritySchemes(AuthConstant.ACCESS_TOKEN_KEY, null)
      );
  }
}
