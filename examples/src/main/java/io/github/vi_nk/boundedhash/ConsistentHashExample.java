package io.github.vi_nk.boundedhash.examples;

import io.github.vi_nk.boundedhash.ConsistentHash;
import io.github.vi_nk.boundedhash.FNV1a64;
import io.github.vi_nk.boundedhash.Hasher;
import io.github.vi_nk.boundedhash.Node;
import io.github.vi_nk.boundedhash.Config;

public class ConsistentHashExample {

    public static void main(String[] args) {
        // Define the configuration for the consistent hash
        int partitionCount = 10; // Total number of partitions
        int vNodes = 3; // Number of virtual nodes per physical node
        double loadFactor = 1.25; // Load factor
        Hasher hasher = new FNV1a64(); // Hashing algorithm

        Config config = new Config(partitionCount, vNodes, loadFactor, hasher);

        ConsistentHash consistentHash = new ConsistentHash(config);

        Node node1 = new Node("Node1");
        Node node2 = new Node("Node2");
        Node node3 = new Node("Node3");

        consistentHash.add(node1);
        consistentHash.add(node2);
        consistentHash.add(node3);

        System.out.println("Nodes added: Node1, Node2, Node3");

        String key = "myKey";
        Node locatedNode = consistentHash.locate(key);
        System.out.println("Key '" + key + "' is mapped to node: " + locatedNode.name());

        Node newNode = new Node("Node4");
        consistentHash.add(newNode);
        System.out.println("Added new node: " + newNode.name());

        Node newLocatedNode = consistentHash.locate(key);
        System.out.println("After adding Node4, key '" + key + "' is now mapped to node: " + newLocatedNode.name());

        consistentHash.remove(node2);
        System.out.println("Removed node: " + node2.name());

        Node finalLocatedNode = consistentHash.locate(key);
        System.out.println("After removing Node2, key '" + key + "' is now mapped to node: " + finalLocatedNode.name());
    }
}