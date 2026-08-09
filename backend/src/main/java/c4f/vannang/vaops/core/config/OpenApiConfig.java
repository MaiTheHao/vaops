package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.core.constant.AuthConstant;
import c4f.vannang.vaops.shared.enumeration.ErrorCode;
import c4f.vannang.vaops.shared.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile("!prod")
@ConditionalOnProperty(value = "vaops.openapi.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

  private static final Map<String, Schema> ERROR_SCHEMAS = ModelConverters.getInstance().readAll(ErrorResponse.class);
  private static final Set<String> SUPPORTED_ERROR_CODES = Set.of("400", "401", "403", "404", "500");

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
        .addSecurityItem(new SecurityRequirement().addList(AuthConstant.ACCESS_TOKEN_KEY));
  }

  @Bean
  public OpenApiCustomizer errorResponseOpenApiCustomizer() {
    String ERROR_REF_SCHEMA = "#/components/schemas/ErrorResponse";

    return openApi -> {
      if (openApi.getComponents() == null) {
        openApi.setComponents(new Components());
      }

      if (ERROR_SCHEMAS != null) {
        ERROR_SCHEMAS.forEach((name, schema) -> openApi.getComponents().addSchemas(name, schema));
      }

      Schema<?> errorRefSchema = new Schema<>().$ref(ERROR_REF_SCHEMA);

      if (openApi.getPaths() != null) {
        openApi
            .getPaths()
            .values()
            .forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
              if (operation.getResponses() != null) {
                operation.getResponses().forEach((code, response) -> {
                  if (response.getContent() != null && !response.getContent().isEmpty()) {
                    return;
                  }

                  if (!SUPPORTED_ERROR_CODES.contains(code)) {
                    if (isErrorCodeCandidate(code)) {
                      log.warn("Skipping dynamic error example generation for unsupported or custom status code: {}", code);
                    }
                    return;
                  }

                  int statusCode = Integer.parseInt(code);
                  Map<String, Object> dynamicExample =
                      buildDynamicErrorExample(statusCode, response.getDescription());

                  MediaType mediaType = new MediaType()
                      .schema(errorRefSchema)
                      .example(dynamicExample);

                  Content content = new Content().addMediaType(
                      org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                      mediaType);
                  response.setContent(content);
                });
              }
            }));
      }
    };
  }

  private boolean isErrorCodeCandidate(String statusCode) {
    try {
      int code = Integer.parseInt(statusCode);
      return code >= 400;
    } catch (NumberFormatException e) {
      return statusCode.startsWith("4") || statusCode.startsWith("5");
    }
  }

  private Map<String, Object> buildDynamicErrorExample(int status, String description) {
    Map<String, Object> example = new LinkedHashMap<>();
    example.put("timestamp", "2026-08-09T13:45:00Z");
    example.put("status", status);

    ErrorCode errorCode = findErrorCodeByStatus(status);
    example.put("code", errorCode != null ? errorCode.code() : "ERROR_" + status);

    String message = (description != null && !description.isBlank())
        ? description
        : (errorCode != null ? errorCode.name() : "An error occurred");
    example.put("message", message);
    example.put("path", "/api/v1/...");
    example.put("requestId", "req-9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d");

    if (status == 400) {
      example.put("details", Map.of("field", "Validation error details"));
    }

    return example;
  }

  private ErrorCode findErrorCodeByStatus(int status) {
    for (ErrorCode ec : ErrorCode.values()) {
      if (ec.status().value() == status) {
        return ec;
      }
    }
    return null;
  }
}
