package c4f.vannang.vaops.core.env;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.app")
public class AppProperties {
    private boolean isProd = false;
}
