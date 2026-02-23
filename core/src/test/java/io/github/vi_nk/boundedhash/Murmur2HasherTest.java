package io.github.vi_nk.boundedhash;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

public class Murmur2HasherTest {

    private final Hasher hasher = new Murmur2Hasher();

    private long hash(String input) {
        return hasher.hash(input.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void testPrecalculatedHashes() {
        assertEquals(-1559804140782952724L, hash("test"), "Failed on 'test'");

        assertEquals(-7467008419718616472L, hash("hello world"), "Failed on 'hello world'");

        assertEquals(-659205437535635261L, hash("NodeA"), "Failed on 'NodeA'");
        assertEquals(-6136276972031207193L, hash("NodeA0"), "Failed on 'NodeA0'");

        assertEquals(2826398438823939239L, hash("user-123"), "Failed on 'user-123'");
    }

    @Test
    void testEmptyAndNullInputs() {
        assertEquals(0L, hasher.hash(null), "Null data should return 0L");
        assertEquals(0L, hasher.hash(new byte[0]), "Empty byte array should return 0L");
    }

    @Test
    void testDeterministicOutput() {
        String key = "consistent-routing-key";
        long firstHash = hash(key);

        for (int i = 0; i < 100; i++) {
            assertEquals(firstHash, hash(key), "Murmur2 must be completely deterministic");
        }
    }

}