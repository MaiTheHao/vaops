package c4f.vannang.vaops.core.env;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vaops.server")
public class ServerProperties {
    private int port;
}
