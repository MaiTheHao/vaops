package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

import c4f.vannang.vaops.core.env.AppProperties;
import c4f.vannang.vaops.modules.authentication.api.AuthenticationModuleApi;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RefreshTokenCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.exception.UnauthenticatedException;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.infrastructure.web.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

  private final AuthenticationModuleApi authModuleApi;
  private final AppProperties appProperties;
  private final AuthProperties authProperties;

  @PostMapping("/login")
  public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto request) {
    LoginCommandResultDto result =
        authModuleApi.login(new LoginCommandDto(request.accountName(), request.password()));

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
  public ResponseEntity<RegisterResponseDto> register(
      @Valid @RequestBody RegisterRequestDto request) {
    RegisterCommandResultDto result = authModuleApi.register(new RegisterCommandDto(
        request.accountName(), request.password(), request.displayName(), request.avatarUrl()));

    RegisterResponseDto response = new RegisterResponseDto(
        result.id(), result.accountName(), result.displayName(), result.avatarUrl());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<Void> refresh(HttpServletRequest request) {
    String refreshTokenValue = extractRefreshTokenFromCookie(request);
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new UnauthenticatedException("Refresh token is missing");
    }

    RefreshTokenCommandResultDto result =
        authModuleApi.refreshToken(new RefreshTokenCommandDto(refreshTokenValue));

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
