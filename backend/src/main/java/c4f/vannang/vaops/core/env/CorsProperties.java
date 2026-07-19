package c4f.vannang.vaops.core.env;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of("http://localhost:4200", "http://localhost:8080");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
    private List<String> allowedHeaders = List.of("Authorization", "Content-Type", "Cache-Control");
    private Boolean allowCredentials = true;
    private List<String> exposedHeaders = List.of();
    private Long maxAge = 3600L;
}
