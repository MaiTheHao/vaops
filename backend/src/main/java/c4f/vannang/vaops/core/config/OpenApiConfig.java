package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.core.constant.AuthConstant;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
@ConditionalOnProperty(value = "vaops.openapi.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .components(new Components()
            .addSecuritySchemes(
                AuthConstant.ACCESS_TOKEN_KEY,
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .name(AuthConstant.ACCESS_TOKEN_KEY))
            .addSecuritySchemes(
                AuthConstant.REFRESH_TOKEN_KEY,
                new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.COOKIE)
                    .name(AuthConstant.REFRESH_TOKEN_KEY)))
        .addSecurityItem(
            new SecurityRequirement()
                .addList(AuthConstant.ACCESS_TOKEN_KEY));
  }
}
