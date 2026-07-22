package c4f.vannang.vaops.shared.token.provider;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenProviderRegistryTest {

    static class DummyJwtProvider implements TokenProvider {
        @Override
        public String getName() {
            return "jwt";
        }
    }

    @Test
    void testGetProviderByName() {
        DummyJwtProvider jwtProvider = new DummyJwtProvider();
        TokenProviderRegistry registry = new TokenProviderRegistry(List.of(jwtProvider));

        assertEquals(jwtProvider, registry.get("jwt"));
        assertEquals(jwtProvider, registry.jwt());
    }

    @Test
    void testGetUnknownProviderThrowsException() {
        TokenProviderRegistry registry = new TokenProviderRegistry(List.of());

        assertThrows(IllegalArgumentException.class, () -> registry.get("unknown"));
    }
}
