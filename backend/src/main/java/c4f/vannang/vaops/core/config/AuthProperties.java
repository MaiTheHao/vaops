package c4f.vannang.vaops.core.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "vaops.auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();

    @PostConstruct
    private void logging() {
        log.debug("AuthProperties: {}", this);
    }

    @Data
    public static class Jwt {
        @ToString.Exclude
        private String accessSecret;
        private long accessExpirationMs = 900_000;
        @ToString.Exclude
        private String refreshSecret;
        private long refreshExpirationMs = 604_800_000;
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

