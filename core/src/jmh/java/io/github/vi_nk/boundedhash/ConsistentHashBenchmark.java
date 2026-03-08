package io.github.vi_nk.boundedhash;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class ConsistentHashBenchmark {

    private ConsistentHash router;
    private String[] testKeys;
    private int index = 0;

    @Setup
    public void setup() {
        // Production-like config
        Config config = new Config(4096, 256, 1.25, new Murmur2Hasher());
        router = new ConsistentHash(config);

        // Add initial nodes
        router.add(new Node("Node-A"));
        router.add(new Node("Node-B"));
        router.add(new Node("Node-C"));

        // Pre-generate keys to avoid measuring UUID.randomUUID() speed
        testKeys = new String[1000];
        for (int i = 0; i < 1000; i++) {
            testKeys[i] = "user-key-" + i;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public Node benchmarkLocate() {
        // Measures the hot path: Hashing -> Partition Lookup -> Array Access
        String key = testKeys[index++ % 1000];
        return router.locate(key);
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void benchmarkAddRemove() {
        // Measures the rebalancing logic (Redistributing 4096 partitions)
        Node tempNode = new Node("TempNode");
        router.add(tempNode);
        router.remove(tempNode);
    }
}