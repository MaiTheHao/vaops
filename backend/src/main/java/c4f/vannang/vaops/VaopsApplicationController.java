package c4f.vannang.vaops;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VaopsApplicationController {

    @GetMapping({"/hello", "/v1/hello"})
    public Map<String, Object> getHello() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return Map.of("name", "VAOPS System", "time", now.toString());
    }



    @GetMapping({"/hello-authed", "/v1/hello-authed"})
    public Map<String, Object> getHelloAuthed(Principal principal) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String name = principal != null ? principal.getName() : "Authenticated User";
        return Map.of("name", "VAOPS System (Authed)", "time", now.toString(), "user", name);
    }
}
