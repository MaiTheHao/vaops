package c4f.vannang.vaops.core.env;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.databases")
public class DatabaseProperties {
    private DatabaseConfig primary = new DatabaseConfig();

    @Data
    public static class DatabaseConfig {
        private String host;
        private int port;
        private String name;
        private String username;
        private String password;
    }
}
