package c4f.vannang.vaops.shared.token.provider;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TokenProviderRegistry {
    private final Map<String, TokenProvider> providers;

    public TokenProviderRegistry(List<TokenProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(
                        p -> p.getName().toLowerCase(),
                        p -> p,
                        (existing, replacement) -> existing));
    }

    public TokenProvider get(String providerName) {
        return Optional.ofNullable(providers.get(providerName.toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("No TokenProvider registered for name: " + providerName));
    }

    public TokenProvider jwt() {
        return get("jwt");
    }

    public TokenProvider paseto() {
        return get("paseto");
    }

    public TokenProvider redis() {
        return get("redis");
    }
}
