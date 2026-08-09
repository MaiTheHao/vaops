package c4f.vannang.vaops.modules.authentication.infrastructure.web.controller;

import c4f.vannang.vaops.core.constant.AuthConstant;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "User login, registration, token refresh, and logout operations")
public class AuthenticationController {

  private final AuthenticationService authenticationService;
  private final AppProperties appProperties;
  private final AuthProperties authProperties;

  @PostMapping("/login")
  @Operation(summary = "User login with account credentials")
  @SecurityRequirements({})
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Login successful (Sets authentication cookies)"),
      @ApiResponse(responseCode = "400", description = "Invalid request payload"),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  public ResponseEntity<Void> login(@Valid @RequestBody LoginWebRequestDto request) {
    LoginCommandResult result =
        authenticationService.login(new LoginCommand(request.accountName(), request.password()));

    ResponseCookie accessCookie = ResponseCookie.from(AuthConstant.ACCESS_TOKEN_KEY, result.accessToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from(AuthConstant.REFRESH_TOKEN_KEY, result.refreshToken())
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
  @Operation(summary = "Register new user account")
  @SecurityRequirements({})
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "User registered successfully"),
      @ApiResponse(responseCode = "400", description = "Validation error or username taken")
  })
  public ResponseEntity<RegisterWebResponseDto> register(
      @Valid @RequestBody RegisterWebRequestDto request) {
    RegisterCommandResult result = authenticationService.register(new RegisterCommand(
        request.accountName(), request.password(), request.displayName(), request.avatarUrl()));

    RegisterWebResponseDto response = new RegisterWebResponseDto(
        result.id(), result.accountName(), result.displayName(), result.avatarUrl());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh access token using refresh cookie")
  @SecurityRequirements({})
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
      @ApiResponse(responseCode = "401", description = "Missing or invalid refresh token")
  })
  public ResponseEntity<Void> refresh(HttpServletRequest request) {
    String refreshTokenValue = extractRefreshTokenFromCookie(request);
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new UnauthenticatedException("Refresh token is missing");
    }

    RefreshTokenCommandResult result =
        authenticationService.refreshToken(new RefreshTokenCommand(refreshTokenValue));

    ResponseCookie accessCookie = ResponseCookie.from(AuthConstant.ACCESS_TOKEN_KEY, result.accessToken())
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(Duration.ofMillis(authProperties.getJwt().getAccessExpirationMs()))
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from(AuthConstant.REFRESH_TOKEN_KEY, result.refreshToken())
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
  @Operation(summary = "User logout and clear session cookies")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Logged out successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthenticated")
  })
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    String refreshTokenValue = extractRefreshTokenFromCookie(request);
    if (refreshTokenValue != null && !refreshTokenValue.isBlank()) {
      authenticationService.logout(new LogoutCommand(refreshTokenValue));
    }

    ResponseCookie accessCookie = ResponseCookie.from(AuthConstant.ACCESS_TOKEN_KEY, "")
        .httpOnly(true)
        .secure(appProperties.isProd())
        .path("/")
        .maxAge(0)
        .sameSite("Lax")
        .build();

    ResponseCookie refreshCookie = ResponseCookie.from(AuthConstant.REFRESH_TOKEN_KEY, "")
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
      if (AuthConstant.REFRESH_TOKEN_KEY.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}

