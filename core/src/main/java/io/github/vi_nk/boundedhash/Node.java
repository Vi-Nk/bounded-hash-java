package io.github.vi_nk.boundedhash;

/**
 * Represents a cluster node with a name and capacity.
 *
 * @param name     unique node name
 * @param capacity relative capacity used for partitioning decisions (must be
 *                 &gt;= 1)
 */
public record Node(String name, int capacity) {
    public Node {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be null or empty");
        if (capacity < 1)
            throw new IllegalArgumentException("capacity must be >= 1");
    }

    /**
     * Convenience constructor using a default capacity of {@code 1}.
     *
     * @param name the node name
     */
    public Node(String name) {
        this(name, 1);
    }
}