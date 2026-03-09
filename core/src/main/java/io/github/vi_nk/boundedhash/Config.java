package io.github.vi_nk.boundedhash;

/**
 * Configuration record for the consistent hash algorithm.
 *
 * @param partitionCount total number of partitions (must be >= 1)
 * @param vNodes         number of virtual nodes per physical node (must be >=
 *                       1)
 * @param loadFactor     desired load factor (epsilon &gt;= 1.0)
 * @param hasher         hashing implementation to use for ring and partition
 *                       hashing
 */
public record Config(
        int partitionCount, // Total buckets
        int vNodes, // VirtualNodeCounts
        double loadFactor, // Epsilon > 1
        Hasher hasher) { // Hashing implementation
    public Config {
        if (partitionCount < 1)
            throw new IllegalArgumentException("partitionCount must be >= 1");
        if (loadFactor < 1.0)
            throw new IllegalArgumentException("loadFactor must be >= 1.0");
        if (hasher == null)
            throw new IllegalArgumentException("hasher cannot be null");
        if (vNodes < 1)
            throw new IllegalArgumentException("vNodes must be >= 1");
    }
}
