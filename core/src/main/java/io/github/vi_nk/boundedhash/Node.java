package io.github.vi_nk.boundedhash;

public record Node(String name, int capacity) {
    public Node {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be null or empty");
        if (capacity < 1)
            throw new IllegalArgumentException("capacity must be >= 1");
    }

    public Node(String name) {
        this(name, 1);
    }
}