package c4f.vannang.vaops.core.env;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.util.List;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "vaops.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of();
    private List<String> allowedMethods = List.of();
    private List<String> allowedHeaders = List.of();
    private Boolean allowCredentials;
    private List<String> exposedHeaders = List.of();
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
