package c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.controller;

import c4f.vannang.vaops.core.env.AppProperties;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.LoginCommandResultDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandDto;
import c4f.vannang.vaops.modules.authentication.api.dto.RegisterCommandResultDto;
import c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.dto.LoginRequestDto;
import c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.dto.RegisterRequestDto;
import c4f.vannang.vaops.modules.authentication.internal.infrastructure.web.dto.RegisterResponseDto;
import c4f.vannang.vaops.modules.authentication.internal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthService authService;
    private final AppProperties appProperties;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequestDto request) {
        LoginCommandResultDto result = authService.login(new LoginCommandDto(request.accountName(), request.password()));

        ResponseCookie accessCookie = ResponseCookie.from("access_token", result.accessToken())
                .httpOnly(true)
                .secure(appProperties.isProd())
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", result.refreshToken())
                .httpOnly(true)
                .secure(appProperties.isProd())
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterCommandResultDto result = authService.register(new RegisterCommandDto(
                request.accountName(),
                request.password(),
                request.displayName(),
                request.avatarUrl()
        ));

        RegisterResponseDto response = new RegisterResponseDto(
                result.id(),
                result.accountName(),
                result.displayName(),
                result.avatarUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
