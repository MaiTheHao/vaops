package c4f.vannang.vaops.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String path,
    String requestId,
    Map<String, Object> details
) {
    public static ErrorResponse of(int status, String code, String message, String path, String requestId, Map<String, Object> details) {
        return new ErrorResponse(Instant.now(), status, code, message, path, requestId, details);
    }
}
