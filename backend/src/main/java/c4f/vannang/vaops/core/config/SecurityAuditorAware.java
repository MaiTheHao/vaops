package c4f.vannang.vaops.core.config;

import c4f.vannang.vaops.shared.security.AuthenticatedPrincipal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("auditorProvider")
public class SecurityAuditorAware implements AuditorAware<UUID> {

  @Override
  public Optional<UUID> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    Object principal = authentication.getPrincipal();
    if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
      return Optional.ofNullable(authenticatedPrincipal.userId());
    }
    return Optional.empty();
  }
}
