package c4f.vannang.vaops.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard platform error response structure")
public record ErrorResponse(

    @Schema(description = "Timestamp when the error occurred")
    Instant timestamp,

    @Schema(description = "HTTP status code")
    int status,

    @Schema(description = "Platform error code from ErrorCode enum")
    String code,

    @Schema(description = "Human readable error message")
    String message,

    @Schema(description = "Request URI path")
    String path,

    @Schema(description = "Unique trace request ID for debugging")
    String requestId,

    @Schema(description = "Detailed field validation errors or context details")
    Map<String, Object> details) {
        
  public static ErrorResponse of(
      int status,
      String code,
      String message,
      String path,
      String requestId,
      Map<String, Object> details) {
    return new ErrorResponse(Instant.now(), status, code, message, path, requestId, details);
  }
}
