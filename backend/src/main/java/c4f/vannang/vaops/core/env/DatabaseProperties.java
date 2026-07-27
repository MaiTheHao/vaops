package c4f.vannang.vaops.core.env;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "vaops.databases")
public class DatabaseProperties {
    private DatabaseConfig primary = new DatabaseConfig();

    @PostConstruct
    private void logging() {
        log.debug("DatabaseProperties: {}", this);
    }

    @Data
    public static class DatabaseConfig {
        private String host;
        private int port;
        private String name;
        private String username;
        @ToString.Exclude
        private String password;

        @Override
        public String toString() {
            return "DatabaseConfig{" +
                    "host='" + host + '\'' +
                    ", port=" + port +
                    ", name='" + name + '\'' +
                    ", username='" + username + '\'' +
                    ", password='***'" +
                    '}';
        }
    }
}
