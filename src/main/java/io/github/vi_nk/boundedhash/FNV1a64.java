package io.github.vi_nk.boundedhash;

public class FNV1a64 implements Hasher {
    private static final long fnv_offset_basis = 0xcbf29ce484222325L;
    private static final long fnv_prime = 0x100000001b3L;

    @Override
    public long hash(byte[] data) {
        long hashValue = fnv_offset_basis;
        for (byte b : data) {
            hashValue ^= b;
            hashValue *= fnv_prime;
        }
        return hashValue;
    }

}
