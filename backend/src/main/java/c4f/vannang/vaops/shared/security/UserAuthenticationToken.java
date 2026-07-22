package c4f.vannang.vaops.shared.security;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class UserAuthenticationToken extends AbstractAuthenticationToken {

  private final AuthenticatedPrincipal principal;

  public UserAuthenticationToken(
      AuthenticatedPrincipal principal,
      Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    this.principal = principal;
    setAuthenticated(true);
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public AuthenticatedPrincipal getPrincipal() {
    return this.principal;
  }

  @Override
  public String getName() {
    return this.principal != null && this.principal.userId() != null
        ? this.principal.userId().toString()
        : super.getName();
  }
}
