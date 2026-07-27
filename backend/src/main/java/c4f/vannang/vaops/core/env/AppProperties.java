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
@ConfigurationProperties(prefix = "vaops.app")
public class AppProperties {
    private boolean isProd;

    @PostConstruct
    private void logging() {
        log.debug("AppProperties: {}", this);
    }
}
