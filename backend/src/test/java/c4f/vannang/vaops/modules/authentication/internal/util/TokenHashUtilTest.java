package c4f.vannang.vaops.modules.authentication.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TokenHashUtilTest {

    @Test
    void hash_shouldReturn64HexChars() {
        String hash = TokenHashUtil.hash("my-refresh-token-value");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void hash_shouldBeDeterministic() {
        String token = "same-token";
        assertEquals(TokenHashUtil.hash(token), TokenHashUtil.hash(token));
    }

    @Test
    void hash_shouldProduceDifferentHashesForDifferentInputs() {
        assertNotEquals(TokenHashUtil.hash("token-a"), TokenHashUtil.hash("token-b"));
    }

    @Test
    void hash_shouldHandleEmptyString() {
        String hash = TokenHashUtil.hash("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void hash_shouldHandleSpecialCharacters() {
        String hash = TokenHashUtil.hash("token-with-special-chars!@#$%^&*()");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}
