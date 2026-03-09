package io.github.vi_nk.boundedhash;

/**
 * Hasher defines a thin interface for hashing byte arrays to a 64-bit value.
 * Implementations should provide fast, well-distributed non-cryptographic
 * hashing suitable for partitioning and consistent hashing.
 */
public interface Hasher {

    /**
     * Compute a 64-bit hash for the provided byte array.
     *
     * @param data the input bytes to hash; may be {@code null} or empty.
     * @return a 64-bit hash value.
     */
    public long hash(byte[] data);

}
