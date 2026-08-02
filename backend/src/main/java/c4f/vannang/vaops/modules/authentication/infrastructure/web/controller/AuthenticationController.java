package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

import c4f.vannang.vaops.core.env.AppProperties;
import c4f.vannang.vaops.core.env.AuthProperties;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterWebResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.LoginCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.LogoutCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RefreshTokenCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommand;
import c4f.vannang.vaops.modules.authentication.internal.dto.RegisterCommandResult;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthenticationService;
import c4f.vannang.vaops.shared.exception.UnauthenticatedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final AppProperties appProperties;
  private final AuthProperties authProperties;

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody LoginWebRequestDto request) {
    LoginCommandResult result =
        authenticationService.login(new LoginCommand(request.accountName(), request.password()));

    ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getRefreshExpirationMs()))
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .build();
  }

  @PostMapping("/register")
  public ResponseEntity<RegisterWebResponseDto> register(
      @Valid @RequestBody RegisterWebRequestDto request) {
    RegisterCommandResult result = authenticationService.register(new RegisterCommand(
        request.accountName(), request.password(), request.displayName(), request.avatarUrl()));

    RegisterWebResponseDto response = new RegisterWebResponseDto(
        result.id(), result.accountName(), result.displayName(), result.avatarUrl());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refresh(HttpServletRequest request) {
    String refreshTokenValue = extractRefreshTokenFromCookie(request);
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new UnauthenticatedException("Refresh token is missing");
    }

    RefreshTokenCommandResult result =
        authenticationService.refreshToken(new RefreshTokenCommand(refreshTokenValue));

    ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getRefreshExpirationMs()))
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .build();
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String refreshTokenValue = extractRefreshTokenFromCookie(request);
    if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
      authenticationService.logout(new LogoutCommand(refreshTokenValue));
    }

    ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .build();
  }

  private String extractRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies()) {
      if ("refresh_token".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
