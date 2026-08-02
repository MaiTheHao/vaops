package c4f.vannang.vaops.shared.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import c4f.vannang.vaops.shared.enumeration.DeterministicHashAlgorithm;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategy;
import c4f.vannang.vaops.shared.feature.crypto.DeterministicHashStrategyFactory;
import c4f.vannang.vaops.shared.feature.crypto.Sha256DeterministicHashStrategy;
import c4f.vannang.vaops.shared.feature.crypto.Sha512DeterministicHashStrategy;

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
    void factory_ShouldReturnSha256Strategy_WhenRequestingSha256() {
        // given
        DeterministicHashAlgorithm algorithm = DeterministicHashAlgorithm.SHA_256;

        // when
        DeterministicHashStrategy strategy = factory.getStrategy(algorithm);

        // then
        assertThat(strategy).isNotNull();
        assertThat(strategy).isInstanceOf(Sha256DeterministicHashStrategy.class);
        assertThat(strategy.getAlgorithm()).isEqualTo(DeterministicHashAlgorithm.SHA_256);
    }

    @Test
    void factory_ShouldReturnSha512Strategy_WhenRequestingSha512() {
        // given
        DeterministicHashAlgorithm algorithm = DeterministicHashAlgorithm.SHA_512;

        // when
        DeterministicHashStrategy strategy = factory.getStrategy(algorithm);

        // then
        assertThat(strategy).isNotNull();
        assertThat(strategy).isInstanceOf(Sha512DeterministicHashStrategy.class);
        assertThat(strategy.getAlgorithm()).isEqualTo(DeterministicHashAlgorithm.SHA_512);
    }

    @Test
    void factory_ShouldThrowIllegalArgumentException_WhenAlgorithmIsUnsupported() {
        // given
        DeterministicHashStrategyFactory emptyFactory = new DeterministicHashStrategyFactory(List.of());

        // when, then
        assertThatThrownBy(() -> emptyFactory.getStrategy(DeterministicHashAlgorithm.SHA_256))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported deterministic hash algorithm: SHA_256");
    }

    @Test
    void sha256_hash_ShouldReturn64HexChars_WhenHashingInput() {
        // given
        String input = "my-refresh-token-value";

        // when
        String hash = sha256Strategy.hash(input);

        // then
        assertThat(hash).isNotNull().hasSize(64);
    }

    @Test
    void sha256_hash_ShouldBeDeterministic_WhenHashingSameInputTwice() {
        // given
        String input = "same-input";

        // when
        String firstHash = sha256Strategy.hash(input);
        String secondHash = sha256Strategy.hash(input);

        // then
        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    void sha256_hash_ShouldProduceDifferentHashes_WhenInputsDiffer() {
        // given
        String inputA = "input-a";
        String inputB = "input-b";

        // when
        String hashA = sha256Strategy.hash(inputA);
        String hashB = sha256Strategy.hash(inputB);

        // then
        assertThat(hashA).isNotEqualTo(hashB);
    }

    @Test
    void sha256_hash_ShouldHandleEmptyString_WhenInputIsEmpty() {
        // given
        String input = "";

        // when
        String hash = sha256Strategy.hash(input);

        // then
        assertThat(hash).isNotNull().hasSize(64);
    }

    @Test
    void sha512_hash_ShouldReturn128HexChars_WhenHashingInput() {
        // given
        String input = "my-refresh-token-value";

        // when
        String hash = sha512Strategy.hash(input);

        // then
        assertThat(hash).isNotNull().hasSize(128);
    }

    @Test
    void sha512_hash_ShouldBeDeterministic_WhenHashingSameInputTwice() {
        // given
        String input = "same-input";

        // when
        String firstHash = sha512Strategy.hash(input);
        String secondHash = sha512Strategy.hash(input);

        // then
        assertThat(firstHash).isEqualTo(secondHash);
    }

    @Test
    void sha512_hash_ShouldProduceDifferentHashes_WhenInputsDiffer() {
        // given
        String inputA = "input-a";
        String inputB = "input-b";

        // when
        String hashA = sha512Strategy.hash(inputA);
        String hashB = sha512Strategy.hash(inputB);

        // then
        assertThat(hashA).isNotEqualTo(hashB);
    }

    @Test
    void sha512_hash_ShouldHandleEmptyString_WhenInputIsEmpty() {
        // given
        String input = "";

        // when
        String hash = sha512Strategy.hash(input);

        // then
        assertThat(hash).isNotNull().hasSize(128);
    }
}