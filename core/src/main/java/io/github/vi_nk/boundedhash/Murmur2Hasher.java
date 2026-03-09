package io.github.vi_nk.boundedhash;

/**
 * Murmur2Hasher provides an implementation of the {@link Hasher} interface
 * using
 * the MurmurHash2 algorithm (64-bit variant). This implementation is suitable
 * for non-cryptographic hashing where speed and distribution quality are
 * important.
 */
public class Murmur2Hasher implements Hasher {
    // https://github.com/aappleby/smhasher/blob/0ff96f7835817a27d0487325b6c16033e2992eb5/src/MurmurHash2.cpp#L96
    private static final long M = 0xc6a4a7935bd1e995L;
    private static final int R = 47;

    private final long seed = 0x1234ABCDL;

    @Override
    /**
     * Compute a 64-bit Murmur2 hash for the provided byte array.
     *
     * @param data the input bytes to hash; may be {@code null} or empty in which
     *             case {@code 0L} is returned.
     * @return the 64-bit hash value as a {@code long}.
     */
    public long hash(byte[] data) {
        if (data == null || data.length == 0) {
            return 0L;
        }

        long h = seed ^ (data.length * M);
        int length = data.length;
        int currentIndex = 0;

        while (length >= 8) {
            long k = ((long) data[currentIndex] & 0xFF)
                    | (((long) data[currentIndex + 1] & 0xFF) << 8)
                    | (((long) data[currentIndex + 2] & 0xFF) << 16)
                    | (((long) data[currentIndex + 3] & 0xFF) << 24)
                    | (((long) data[currentIndex + 4] & 0xFF) << 32)
                    | (((long) data[currentIndex + 5] & 0xFF) << 40)
                    | (((long) data[currentIndex + 6] & 0xFF) << 48)
                    | (((long) data[currentIndex + 7] & 0xFF) << 56);

            k *= M;
            k ^= k >>> R;
            k *= M;

            h ^= k;
            h *= M;

            currentIndex += 8;
            length -= 8;
        }

        switch (length) {
            case 7:
                h ^= ((long) data[currentIndex + 6] & 0xFF) << 48;
            case 6:
                h ^= ((long) data[currentIndex + 5] & 0xFF) << 40;
            case 5:
                h ^= ((long) data[currentIndex + 4] & 0xFF) << 32;
            case 4:
                h ^= ((long) data[currentIndex + 3] & 0xFF) << 24;
            case 3:
                h ^= ((long) data[currentIndex + 2] & 0xFF) << 16;
            case 2:
                h ^= ((long) data[currentIndex + 1] & 0xFF) << 8;
            case 1:
                h ^= ((long) data[currentIndex] & 0xFF);
                h *= M;
        }
        h ^= h >>> R;
        h *= M;
        h ^= h >>> R;

        return h;
    }
}
