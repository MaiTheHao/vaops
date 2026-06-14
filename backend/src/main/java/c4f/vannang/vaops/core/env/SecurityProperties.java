package c4f.vannang.vaops.core.env;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.security")
public class SecurityProperties {
    private Token token = new Token();

    @Data
    public static class Token {
        private TokenConfig access = new TokenConfig();
        private TokenConfig refresh = new TokenConfig();

        @Data
        public static class TokenConfig {
            private String secret;
            private long expiration;
        }
    }
}
