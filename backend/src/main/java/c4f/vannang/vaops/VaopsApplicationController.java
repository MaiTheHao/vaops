package c4f.vannang.vaops;

import c4f.vannang.vaops.shared.feature.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System Health", description = "Public & authenticated system check endpoints")
public class VaopsApplicationController {

    @GetMapping({"/hello", "/api/v1/hello"})
    @Operation(summary = "Public health check endpoint")
    @SecurityRequirements({})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "System health status payload")
    })
    public Map<String, Object> getHello() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return Map.of("name", "VAOPS System", "time", now.toString());
    }

    @GetMapping({"/hello-authed", "/api/v1/hello-authed"})
    @Operation(summary = "Authenticated health check endpoint")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated system health status payload"),
        @ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    public Map<String, Object> getHelloAuthed(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String name = principal != null ? principal.accountName() : "Authenticated User";
        return Map.of("name", "VAOPS System (Authed)", "time", now.toString(), "user", name);
    }
}

