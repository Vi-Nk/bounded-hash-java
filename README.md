# Bounded Hash Java Library

## Overview

Consistent hashing with bounded loads is a load balancing technique that ensures uniform distribution of clients across servers while maintaining stability and minimizing client reassignment during system changes. By introducing a load factor (ε), it guarantees that no server exceeds (1+ε) times the average load, achieving both uniformity and consistency. For more details, refer to the [Google Research blog](https://research.google/blog/consistent-hashing-with-bounded-loads/).

The algorithm provides tight guarantees on the maximum load of each server while maintaining consistency and stability. It minimizes the number of client reassignments when changes occur in the system, such as the addition or removal of servers or clients. This makes it ideal for scenarios where both uniformity and consistency in load distribution are critical.

This library implements the principles of consistent hashing with bounded loads, providing a robust solution for load balancing, caching, and distributed systems.

## Installation

WIP

## Usage

### Example
Below is an example of how to use the `ConsistentHash` class:

```java
import io.github.vi_nk.boundedhash.*;
import java.util.Map;

public class ConsistentHashExample {
    public static void main(String[] args) {
        Config config = new Config(10, 3, 1.25, new FNV1a64());
        ConsistentHash consistentHash = new ConsistentHash(config);

        Node node1 = new Node("Node1");
        Node node2 = new Node("Node2");
        Node node3 = new Node("Node3");

        consistentHash.add(node1);
        consistentHash.add(node2);
        consistentHash.add(node3);

        String key = "myKey";
        Node locatedNode = consistentHash.locate(key);
        System.out.println("Key '" + key + "' is mapped to node: " + locatedNode.name());

        Map<String, Integer> loadDistribution = consistentHash.getLoadDistribution();
        System.out.println("Load distribution across nodes: " + loadDistribution);
    }
}
```

## Development

### Prerequisites
- Java 17 or higher
- Gradle 8.5 or higher

### Building the Project
To build the project, run the following command:

```bash
./gradlew build
```

### Running Tests
To execute the test suite, use the following command:

```bash
./gradlew test
```

### Running Examples
To run the example provided in the `examples` module, use the following command:

```bash
cd examples
./gradlew run
```
