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
@ConfigurationProperties(prefix = "vaops.databases")
public class DatabaseProperties {

    @Valid
    @NotNull
    private DatabaseConfig primary = new DatabaseConfig();

    @PostConstruct
    private void logging() {
        log.debug("DatabaseProperties: {}", this);
    }

    @Data
    public static class DatabaseConfig {

        @NotBlank(message = "vaops.databases.primary.host is required")
        private String host;

        @NotNull(message = "vaops.databases.primary.port is required")
        private Integer port;

        @NotBlank(message = "vaops.databases.primary.name is required")
        private String name;

        @NotBlank(message = "vaops.databases.primary.username is required")
        private String username;

        @NotBlank(message = "vaops.databases.primary.password is required")
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
