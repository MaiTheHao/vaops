package c4f.vannang.vaops;

import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaopsApplicationController {

    @GetMapping({"/hello", "/api/v1/hello"})
    public Map<String, Object> getHello() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return Map.of("name", "VAOPS System", "time", now.toString());
    }

    @GetMapping({"/hello-authed", "/api/v1/hello-authed"})
    public Map<String, Object> getHelloAuthed(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String name = principal != null ? principal.accountName() : "Authenticated User";
        return Map.of("name", "VAOPS System (Authed)", "time", now.toString(), "user", name);
    }
}
