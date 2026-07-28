package c4f.vannang.vaops.core.env;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Slf4j
@Component
@Validated
@ConfigurationProperties(prefix = "vaops.cors")
public class CorsProperties {

    @NotNull(message = "vaops.cors.allowed-origins is required")
    private List<String> allowedOrigins;

    @NotNull(message = "vaops.cors.allowed-methods is required")
    private List<String> allowedMethods;

    @NotNull(message = "vaops.cors.allowed-headers is required")
    private List<String> allowedHeaders;

    @NotNull(message = "vaops.cors.allow-credentials is required")
    private Boolean allowCredentials;

    @NotNull(message = "vaops.cors.exposed-headers is required")
    private List<String> exposedHeaders;

    @NotNull(message = "vaops.cors.max-age is required")
    private Long maxAge;

    @PostConstruct
    private void logging() {
        log.debug("CorsProperties: {}", this);
    }

    @Override
    public String toString() {
        return "CorsProperties{" +
                "allowedOrigins=" + allowedOrigins +
                ", allowedMethods=" + allowedMethods +
                ", allowedHeaders=" + allowedHeaders +
                ", allowCredentials=" + allowCredentials +
                ", exposedHeaders=" + exposedHeaders +
                ", maxAge=" + maxAge +
                '}';
    }
}
