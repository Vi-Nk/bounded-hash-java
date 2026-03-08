# Bounded Hash Java

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A zero-dependency, thread-safe Java implementation of Consistent Hashing with Bounded Loads. 

Based on the algorithm formalized by Google Research and utilized by Vimeo engineering, this library provides uniform routing across distributed systems while strictly capping the maximum assignment load on any single node. It mitigates the hotspotting issues prevalent in traditional consistent hashing algorithms.

## Features

* **Bounded Loads**: Implements a configurable load factor to guarantee no server receives more than `(1 + ε)` times the average load.
* **Thread-Safe**: Utilizes `ReentrantReadWriteLock` to support high-throughput concurrent reads alongside safe, blocking writes during topology changes.
* **Zero Dependencies**: Lightweight architecture with no external library requirements.
* **High-Performance Hashing**: Includes an optimized, allocation-free 64-bit `Murmur2Hasher` for uniform distribution. The library provides a default hasher but is extendable to any other hashing algorithm of the user's choice.

## Installation

### Maven


### Gradle


## Usage

You can find a complete example in the [`examples` folder](examples/src/main/java/io/github/vi_nk/boundedhash/ConsistentHashExample.java). Below is a concise implementation for configuring the router, defining nodes, and locating assigned destinations.

### Example Code
```java
import io.github.vi_nk.boundedhash.*;

public class ConsistentHashExample {
    public static void main(String[] args) {
        // Initialize configuration
        ConsistentHash router = new ConsistentHash(new Config(4096, 256, 1.25, new Murmur2Hasher()));

        // Register nodes
        router.add(new Node("Server-A"));
        router.add(new Node("Server-B"));
        router.add(new Node("Server-C"));

        // Route keys to their designated nodes
        System.out.println("Key 'session-1234' -> Node: " + router.locate("session-1234").name());
        System.out.println("Key 'session-9999' -> Node: " + router.locate("session-9999").name());

        // Retrieve and print partition distribution
        System.out.println("Partition Distribution: " + router.getLoadDistribution());
    }
}
```

### Running the Example

```bash
./gradlew :examples:run
```

## Development

### Prerequisites

* Java 17 or higher
* Gradle 8.5 or higher

### Building the Project

Compile the source code and build the JAR:

```bash
./gradlew build
```

### Running Tests

Execute the JUnit test suite to verify bounded load constraints and thread-safety:

```bash
./gradlew test
```

## Benchmark Results

The following benchmark results demonstrate the performance of the `ConsistentHash` implementation. Benchmarks were run using JMH (Java Microbenchmark Harness) on a JDK 17 environment.

### Configuration Used
- **Partitions**: 4096
- **Virtual Nodes (vNodes)**: 256
- **Load Factor**: 1.25
- **Hasher**: Murmur2Hasher

### Results

| Benchmark                                   | Mode | Cnt | Score       | Error      | Units   |
|--------------------------------------------|------|-----|-------------|------------|---------|
| `ConsistentHashBenchmark.benchmarkAddRemove` | avgt | 5   | 693,831.902 |  17,645.205 | ns/op   |
| `ConsistentHashBenchmark.benchmarkLocate`    | avgt | 4   | 33.720      |  0.280     | ns/op   |
