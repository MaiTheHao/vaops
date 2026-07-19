package c4f.vannang.vaops.modules.authentication.internal;

import org.springframework.stereotype.Component;

import c4f.vannang.vaops.modules.authentication.internal.enumeration.TokenType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TokenProviderFactory {

    private final Map<TokenType, TokenProviderStrategy> services;

    public TokenProviderFactory(List<TokenProviderStrategy> tokenServices) {
        this.services = tokenServices.stream()
                .collect(Collectors.toMap((TokenProviderStrategy s) -> s.getType(), s -> s));
    }

    public TokenProviderStrategy getService(TokenType type) {
        return Optional.ofNullable(services.get(type)).orElseThrow(
                () -> new IllegalArgumentException("No TokenService implementation registered for type: " + type));
    }
}
