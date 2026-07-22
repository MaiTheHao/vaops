package c4f.vannang.vaops.shared.service;

import static org.junit.jupiter.api.Assertions.*;

import c4f.vannang.vaops.shared.crypto.DeterministicHashStrategy;
import c4f.vannang.vaops.shared.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.crypto.Sha256DeterministicHashStrategy;
import c4f.vannang.vaops.shared.crypto.Sha512DeterministicHashStrategy;
import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeterministicHashStrategyTest {

    private Sha256DeterministicHashStrategy sha256Strategy;
    private Sha512DeterministicHashStrategy sha512Strategy;
    private DeterministicHashStrategyFactory factory;

    @BeforeEach
    void setUp() {
        sha256Strategy = new Sha256DeterministicHashStrategy();
        sha512Strategy = new Sha512DeterministicHashStrategy();
        factory = new DeterministicHashStrategyFactory(List.of(sha256Strategy, sha512Strategy));
    }

    @Test
    void factory_shouldReturnCorrectStrategy() {
        DeterministicHashStrategy strategy256 = factory.getStrategy(DeterministicHashAlgorithm.SHA_256);
        assertNotNull(strategy256);
        assertEquals(DeterministicHashAlgorithm.SHA_256, strategy256.getAlgorithm());
        assertTrue(strategy256 instanceof Sha256DeterministicHashStrategy);

        DeterministicHashStrategy strategy512 = factory.getStrategy(DeterministicHashAlgorithm.SHA_512);
        assertNotNull(strategy512);
        assertEquals(DeterministicHashAlgorithm.SHA_512, strategy512.getAlgorithm());
        assertTrue(strategy512 instanceof Sha512DeterministicHashStrategy);
    }

    @Test
    void factory_shouldThrowExceptionForUnsupportedAlgorithm() {
        DeterministicHashStrategyFactory emptyFactory = new DeterministicHashStrategyFactory(List.of());
        assertThrows(IllegalArgumentException.class, () -> emptyFactory.getStrategy(DeterministicHashAlgorithm.SHA_256));
    }

    @Test
    void sha256_hash_shouldReturn64HexChars() {
        String hash = sha256Strategy.hash("my-refresh-token-value");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void sha256_hash_shouldBeDeterministic() {
        String input = "same-input";
        assertEquals(sha256Strategy.hash(input), sha256Strategy.hash(input));
    }

    @Test
    void sha256_hash_shouldProduceDifferentHashesForDifferentInputs() {
        assertNotEquals(sha256Strategy.hash("input-a"), sha256Strategy.hash("input-b"));
    }

    @Test
    void sha256_hash_shouldHandleEmptyString() {
        String hash = sha256Strategy.hash("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void sha512_hash_shouldReturn128HexChars() {
        String hash = sha512Strategy.hash("my-refresh-token-value");
        assertNotNull(hash);
        assertEquals(128, hash.length());
    }

    @Test
    void sha512_hash_shouldBeDeterministic() {
        String input = "same-input";
        assertEquals(sha512Strategy.hash(input), sha512Strategy.hash(input));
    }

    @Test
    void sha512_hash_shouldProduceDifferentHashesForDifferentInputs() {
        assertNotEquals(sha512Strategy.hash("input-a"), sha512Strategy.hash("input-b"));
    }

    @Test
    void sha512_hash_shouldHandleEmptyString() {
        String hash = sha512Strategy.hash("");
        assertNotNull(hash);
        assertEquals(128, hash.length());
    }
}
