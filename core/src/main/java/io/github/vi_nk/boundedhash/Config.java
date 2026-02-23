package io.github.vi_nk.boundedhash;

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
