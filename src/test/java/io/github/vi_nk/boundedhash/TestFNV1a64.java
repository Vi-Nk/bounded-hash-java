package io.github.vi_nk.boundedhash;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestFNV1a64 {

    private final FNV1a64 hasherFnv1a64 = new FNV1a64();

    @Test
    void testEmptyByteArray() {
        String testString = "";
        long expectedHash = 0xcbf29ce484222325L;
        assertEquals(expectedHash, hasherFnv1a64.hash(testString.getBytes()));
    }

    @Test
    void testSingleByte() {
        byte[] data = { 0x61 }; // 'a'
        long expectedHash = 0xaf63dc4c8601ec8cL;
        assertEquals(expectedHash, hasherFnv1a64.hash(data));
    }

    @Test
    void testString() {
        byte[] data = "bounded-hash".getBytes();
        long expectedHash = 0xe19099982da54471L;
        assertEquals(expectedHash, hasherFnv1a64.hash(data));
    }

}
