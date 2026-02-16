package io.github.vi_nk.boundedhash;

public record Config(
        int replicationFactor,
        double loadFactor,
        Hasher hasher) {
    public Config {
        if (replicationFactor < 1)
            throw new IllegalArgumentException("replicationFactor must be >= 1");
        if (loadFactor < 1.0)
            throw new IllegalArgumentException("loadFactor must be >= 1.0");
        if (hasher == null)
            throw new IllegalArgumentException("hasher cannot be null");
    }
}
