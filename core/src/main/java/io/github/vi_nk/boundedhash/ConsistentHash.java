package io.github.vi_nk.boundedhash;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ConsistentHash implements a bounded consistent hashing ring with fixed
 * partitions.
 * <p>
 * The class assigns partition ownership to nodes using a hash ring composed of
 * virtual
 * nodes. It supports adding and removing nodes and locating the owner for a
 * given key.
 * The implementation maintains a stable distribution of partitions and attempts
 * to
 * respect a configurable {@code loadFactor} when assigning partitions to nodes.
 */
public class ConsistentHash {

    private Config config;
    private final Node[] partitionOwner;
    private Set<Node> activeNodes;
    private TreeMap<Long, Node> ring;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Create a new {@code ConsistentHash} with the provided configuration.
     *
     * @param config the configuration describing partition count, virtual nodes,
     *               hasher
     *               and load factor. Must not be {@code null} (validated by
     *               {@code Config}).
     */
    public ConsistentHash(Config config) {
        this.config = config;
        partitionOwner = new Node[config.partitionCount()];
        activeNodes = new HashSet<>();
        ring = new TreeMap<>();

    }

    /**
     * Add a node to the cluster and redistribute partition ownership accordingly.
     * <p>
     * If the node already exists this method is a no-op. The method registers
     * {@code vNodes} virtual nodes for the provided node on the hash ring and then
     * triggers partition distribution to respect the configured load factor.
     *
     * @param node the node to add; must be non-null and have valid properties.
     */
    public void add(Node node) {
        rwLock.writeLock().lock();
        try {
            if (!activeNodes.add(node)) {
                return;
            }
            for (int i = 0; i < config.vNodes(); i++) {
                String vNodeKey = node.name() + i;
                long hashPosition = config.hasher().hash(vNodeKey.getBytes(StandardCharsets.UTF_8));
                ring.put(hashPosition, node);
            }

            distributePartitions();

        } finally {
            rwLock.writeLock().unlock();
        }

    }

    private void distributePartitions() {
        if (activeNodes.isEmpty()) {
            return;
        }

        int maxLoad = (int) Math.ceil(((double) config.partitionCount() / activeNodes.size()) * config.loadFactor());
        Map<Node, Integer> loads = new HashMap<>();
        for (Node n : activeNodes) {
            loads.put(n, 0);
        }

        ByteBuffer buffer = ByteBuffer.allocate(4);
        for (int partId = 0; partId < config.partitionCount(); partId++) {

            buffer.clear();
            buffer.putInt(partId);
            long partHash = config.hasher().hash(buffer.array());

            Map.Entry<Long, Node> currentEntry = ring.ceilingEntry(partHash);
            if (currentEntry == null) {
                currentEntry = ring.firstEntry();
            }

            int nodesChecked = 0;
            while (true) {
                if (nodesChecked >= ring.size()) {
                    throw new IllegalStateException("Not enough room to distribute partitions. Increase loadFactor.");
                }

                Node candidate = currentEntry.getValue();

                if (loads.get(candidate) < maxLoad) {
                    partitionOwner[partId] = candidate;
                    loads.put(candidate, loads.get(candidate) + 1);
                    break;
                }

                currentEntry = ring.higherEntry(currentEntry.getKey());
                if (currentEntry == null) {
                    currentEntry = ring.firstEntry();
                }
                nodesChecked++;
            }
        }

    }

    /**
     * Remove a node from the cluster and redistribute partitions previously owned
     * by that node.
     *
     * @param node the node to remove; if {@code null} or not present this method is
     *             a no-op.
     */
    public void remove(Node node) {
        rwLock.writeLock().lock();
        try {
            if (node == null || !activeNodes.contains(node)) {
                return;
            }

            activeNodes.remove(node);

            for (int i = 0; i < config.vNodes(); i++) {
                String vNodeKey = node.name() + i;
                long hashPosition = config.hasher().hash(vNodeKey.getBytes(StandardCharsets.UTF_8));
                ring.remove(hashPosition);
            }

            if (activeNodes.isEmpty()) {
                Arrays.fill(partitionOwner, null);
            } else {
                distributePartitions();
            }
        } finally {
            rwLock.writeLock().unlock();
        }

    }

    /**
     * Locate the node responsible for the provided key (String form).
     *
     * @param key the key to locate; if {@code null} or empty, {@code null} is
     *            returned.
     * @return the {@link Node} responsible for the key or {@code null} if no nodes
     *         exist.
     */
    public Node locate(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return locate(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Locate the node responsible for the provided key (byte[] form).
     *
     * @param key the key bytes to locate; may be {@code null} in which case
     *            {@code null}
     *            is returned.
     * @return the {@link Node} responsible for the key or {@code null} if no nodes
     *         exist.
     */
    public Node locate(byte[] key) {
        if (key == null || partitionOwner.length == 0) {
            return null;
        }
        rwLock.readLock().lock();
        try {
            long keyHash = config.hasher().hash(key);

            int partitionId = (int) (Long.remainderUnsigned(keyHash, config.partitionCount()));
            return partitionOwner[partitionId];

        } finally {
            rwLock.readLock().unlock();
        }

    }

    /**
     * Return a mapping of node names to the number of partitions currently assigned
     * to each node.
     *
     * @return an immutable map of node name &rarr; partition count, or an empty map
     *         if
     *         there are no active nodes.
     */
    public Map<String, Integer> getLoadDistribution() {
        if (activeNodes.size() == 0)
            return Collections.emptyMap();
        Map<String, Integer> loadDistribution = new HashMap<>(activeNodes.size());

        for (Node n : partitionOwner) {
            if (n != null) {
                int count = loadDistribution.getOrDefault(n.name(), 0);
                loadDistribution.put(n.name(), count + 1);
            }

        }
        return loadDistribution;

    }

}
