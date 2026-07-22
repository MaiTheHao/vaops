package c4f.vannang.vaops.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.auth")
public class AuthProperties {

    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String accessSecret;
        private long accessExpirationMs = 900_000;
        private String refreshSecret;
        private long refreshExpirationMs = 604_800_000;
        private String issuer = "vaops";
    }
}
