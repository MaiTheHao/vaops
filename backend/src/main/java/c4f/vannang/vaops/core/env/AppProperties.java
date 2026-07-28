package c4f.vannang.vaops.core.env;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Slf4j
@Component
@Validated
@ConfigurationProperties(prefix = "vaops.app")
public class AppProperties {

    @NotNull(message = "vaops.app.is-prod is required")
    private Boolean isProd;

    public boolean isProd() {
        return Boolean.TRUE.equals(isProd);
    }

    @PostConstruct
    private void logging() {
        log.debug("AppProperties: {}", this);
    }
}
