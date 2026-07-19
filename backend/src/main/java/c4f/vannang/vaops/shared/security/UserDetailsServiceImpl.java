package c4f.vannang.vaops.shared.security;

import c4f.vannang.vaops.shared.exception.AccountLockedException;
import c4f.vannang.vaops.modules.identity.api.dto.FindForAuthQuery;
import c4f.vannang.vaops.modules.identity.api.dto.UserAuthDto;
import c4f.vannang.vaops.modules.identity.api.service.IdentityModuleApi;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
class UserDetailsServiceImpl implements UserDetailsService {

    private final IdentityModuleApi identityModuleApi;

    @Override
    public UserDetails loadUserByUsername(String accountName) throws UsernameNotFoundException {
        UserAuthDto userAuth = identityModuleApi.getUserForAuth(new FindForAuthQuery(accountName))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + accountName));

        if (!userAuth.active()) {
            throw new UsernameNotFoundException("User account is deactivated");
        }

        if (userAuth.lockedUntil() != null && Instant.now().isBefore(userAuth.lockedUntil())) {
            throw new AccountLockedException("Account is locked until " + userAuth.lockedUntil());
        }

        return User.builder()
                .username(userAuth.id().toString())
                .password(userAuth.passwordHash())
                .authorities(Collections.emptyList())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!userAuth.active())
                .build();
    }
}
