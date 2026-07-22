package c4f.vannang.vaops.shared.filter;

import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.shared.exception.TokenExpiredException;
import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import c4f.vannang.vaops.shared.token.claims.AccessTokenClaims;
import c4f.vannang.vaops.shared.token.specification.AccessTokenSpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AccessTokenSpec accessTokenSpec;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(AccessTokenSpec accessTokenSpec,
            UserDetailsService userDetailsService,
            ObjectMapper objectMapper) {
        this.accessTokenSpec = accessTokenSpec;
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                AccessTokenClaims claims = accessTokenSpec.validateAccessToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(claims.accountName());

                AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
                        UUID.fromString(userDetails.getUsername()),
                        claims.accountName());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                writeErrorResponse(response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private void writeErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (e instanceof TokenExpiredException) {
            response.setStatus(401);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 401,
                    "code", "TOKEN_EXPIRED",
                    "message", e.getMessage()));
        } else if (e instanceof AccountLockedException) {
            response.setStatus(423);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 423,
                    "code", "ACCOUNT_LOCKED",
                    "message", e.getMessage()));
        } else {
            response.setStatus(401);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 401,
                    "code", "AUTHENTICATION_FAILED",
                    "message", "Invalid or expired token"));
        }
    }
}
