package c4f.vannang.vaops.core.web.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import c4f.vannang.vaops.shared.exception.ErrorResponse;
import c4f.vannang.vaops.shared.exception.ExternalServiceException;
import c4f.vannang.vaops.shared.exception.InternalServerException;
import c4f.vannang.vaops.shared.exception.ResourceAlreadyExistsException;
import c4f.vannang.vaops.shared.exception.TimeoutException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PlatformExceptionHandlerTest {

  private static final String MDC_KEY = "requestId";
  private static final String REQUEST_ID = UUID.randomUUID().toString();

  private PlatformExceptionHandler handler;

  @Mock private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new PlatformExceptionHandler();
    MDC.put(MDC_KEY, REQUEST_ID);
    when(request.getRequestURI()).thenReturn("/api/resource");
  }

  @AfterEach
  void tearDown() {
    MDC.remove(MDC_KEY);
  }

  @Test
  void handleInternalServer_ShouldReturnGenericMessageAndNullDetails_When5xx() {
    // given
    InternalServerException ex =
        new InternalServerException(
            "Sensitive internal message with db credentials",
            Map.of("db.url", "jdbc:postgresql://internal-host/db"));

    // when
    ResponseEntity<ErrorResponse> response = handler.handleInternalServer(ex, request);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("INTERNAL_ERROR");
    assertThat(body.message())
        .isEqualTo("An unexpected error occurred. Please try again later.");
    assertThat(body.message()).doesNotContain("Sensitive internal message");
    assertThat(body.details()).isNull();
  }

  @Test
  void handlePlatform_ShouldPassThroughMessageAndDetails_When4xx() {
    // given
    ResourceAlreadyExistsException ex =
        new ResourceAlreadyExistsException(
            "User name 'admin' already exists", Map.of("field", "username"));

    // when
    ResponseEntity<ErrorResponse> response = handler.handlePlatform(ex, request);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("RESOURCE_ALREADY_EXISTS");
    assertThat(body.message()).isEqualTo("User name 'admin' already exists");
    assertThat(body.details()).containsEntry("field", "username");
  }

  @Test
  void handlePlatform_ShouldReturnExternalServiceMessage_WhenExternalServiceException() {
    // given
    ExternalServiceException ex =
        new ExternalServiceException(
            "Upstream vendor API returned 503 with connection stack trace",
            Map.of("upstream.url", "https://internal-vendor.example.com"));

    // when
    ResponseEntity<ErrorResponse> response = handler.handlePlatform(ex, request);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("EXTERNAL_SERVICE_ERROR");
    assertThat(body.message()).isEqualTo("External service temporarily unavailable.");
    assertThat(body.message()).doesNotContain("Upstream vendor API");
    assertThat(body.details()).isNull();
  }

  @Test
  void handlePlatform_ShouldReturnTimeoutMessage_WhenTimeoutException() {
    // given
    TimeoutException ex =
        new TimeoutException(
            "Timed out after 30s connecting to internal db host",
            Map.of("endpoint", "internal-timeout-host"));

    // when
    ResponseEntity<ErrorResponse> response = handler.handlePlatform(ex, request);

    // then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    ErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.code()).isEqualTo("TIMEOUT");
    assertThat(body.message()).isEqualTo("The request timed out. Please try again.");
    assertThat(body.message()).doesNotContain("internal db host");
    assertThat(body.details()).isNull();
  }
}
