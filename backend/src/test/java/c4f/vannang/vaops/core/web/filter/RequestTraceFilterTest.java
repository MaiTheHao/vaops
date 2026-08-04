package c4f.vannang.vaops.core.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestTraceFilterTest {

  private RequestTraceFilter filter;

  @BeforeEach
  void setUp() {
    filter = new RequestTraceFilter();
  }

  @Test
  void doFilterInternal_ShouldKeepValidUuid_WhenValidHeaderProvided() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();
    String validUuid = UUID.randomUUID().toString();
    request.addHeader(RequestTraceFilter.REQUEST_ID_HEADER, validUuid);

    // when
    filter.doFilter(request, response, filterChain);

    // then
    String actualHeader = response.getHeader(RequestTraceFilter.REQUEST_ID_HEADER);
    assertThat(actualHeader).isEqualTo(validUuid);
  }

  @Test
  void doFilterInternal_ShouldGenerateNewUuid_WhenHeaderMissing() throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    // when
    filter.doFilter(request, response, filterChain);

    // then
    String actualHeader = response.getHeader(RequestTraceFilter.REQUEST_ID_HEADER);
    assertThat(actualHeader).isNotNull();
    assertThat(UUID.fromString(actualHeader)).isNotNull();
  }

  @Test
  void doFilterInternal_ShouldReplaceWithNewUuid_WhenHeaderContainsLogInjectionPayload()
      throws ServletException, IOException {
    // given
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();
    request.addHeader(RequestTraceFilter.REQUEST_ID_HEADER, "invalid-id\r\nFAKE_LOG_ENTRY");

    // when
    filter.doFilter(request, response, filterChain);

    // then
    String actualHeader = response.getHeader(RequestTraceFilter.REQUEST_ID_HEADER);
    assertThat(actualHeader).isNotEqualTo("invalid-id\r\nFAKE_LOG_ENTRY");
    assertThat(actualHeader).doesNotContain("\r", "\n");
    assertThat(UUID.fromString(actualHeader)).isNotNull();
  }
}
