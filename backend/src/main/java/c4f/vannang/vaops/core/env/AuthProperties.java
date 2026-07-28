package c4f.vannang.vaops.core.env;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Slf4j
@Component
@Validated
@ConfigurationProperties(prefix = "vaops.auth")
public class AuthProperties {

    @Valid
    @NotNull
    private Jwt jwt = new Jwt();

    @PostConstruct
    private void logging() {
        log.debug("AuthProperties: {}", this);
    }

    @Data
    public static class Jwt {
        
        @NotBlank(message = "vaops.auth.jwt.access-secret is required")
        @ToString.Exclude
        private String accessSecret;

        @NotNull(message = "vaops.auth.jwt.access-expiration-ms is required")
        private long accessExpirationMs;

        @NotBlank(message = "vaops.auth.jwt.refresh-secret is required")
        @ToString.Exclude
        private String refreshSecret;

        @NotNull(message = "vaops.auth.jwt.refresh-expiration-ms is required")
        private long refreshExpirationMs;
        
        @NotBlank(message = "vaops.auth.jwt.issuer is required")
        private String issuer = "vaops";

        @Override
        public String toString() {
            return "Jwt{" +
                    "accessSecret='***'" +
                    ", accessExpirationMs=" + accessExpirationMs +
                    ", refreshSecret='***'" +
                    ", refreshExpirationMs=" + refreshExpirationMs +
                    ", issuer='" + issuer + '\'' +
                    '}';
        }
    }
}

