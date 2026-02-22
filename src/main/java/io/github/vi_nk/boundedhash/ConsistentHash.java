package io.github.vi_nk.boundedhash;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ConsistentHash {

    private Config config;
    private final Node[] partitionOwner;
    private Set<Node> activeNodes;
    private TreeMap<Long, Node> ring;

    public ConsistentHash(Config config) {
        this.config = config;
        partitionOwner = new Node[config.partitionCount()];
        activeNodes = new HashSet<>();
        ring = new TreeMap<>();

    }

    public void add(Node node) {
        if (!activeNodes.add(node)) {
            return;
        }
        for (int i = 0; i < config.vNodes(); i++) {
            String vNodeKey = node.name() + i;
            long hashPosition = config.hasher().hash(vNodeKey.getBytes(StandardCharsets.UTF_8));
            ring.put(hashPosition, node);
        }

        distributePartitions();

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

    public void remove(Node node) {
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
    }

    public Node locate(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return locate(key.getBytes(StandardCharsets.UTF_8));
    }

    public Node locate(byte[] key) {
        if (key == null || partitionOwner.length == 0) {
            return null;
        }

        long keyHash = config.hasher().hash(key);

        int partitionId = (int) (Long.remainderUnsigned(keyHash, config.partitionCount()));
        return partitionOwner[partitionId];
    }

    // public Map<String, Integer> getLoadDistribution() { }

}
