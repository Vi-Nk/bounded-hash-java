package io.github.vi_nk.boundedhash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class TestConsistentHash {

    private Config config;
    private ConsistentHash router;
    private static int PARTITION_COUNT = 1200;
    private static int VNODES = 100;
    private static double LOAD_FACTOR = 1.25;

    @BeforeEach
    void setUp() {
        config = new Config(PARTITION_COUNT, VNODES, LOAD_FACTOR, new Murmur2Hasher());
        router = new ConsistentHash(config);
    }

    @Test
    void testEmptyClusterLocateReturnsNull() {
        assertNull(router.locate("some-key"));
        assertEquals(Collections.emptyMap(), router.getLoadDistribution());
    }

    @Test
    void testSingleNodeOwnsEverything() {
        Node nodeA = new Node("NodeA");
        router.add(nodeA);

        Map<String, Integer> distribution = router.getLoadDistribution();

        assertNotNull(distribution);
        assertEquals(1, distribution.size(), "Only one node should be in the distribution map");
        assertEquals(PARTITION_COUNT, distribution.get("NodeA"));

        assertEquals(nodeA, router.locate("user-123"));
        assertEquals(nodeA, router.locate("test-file.txt"));
    }

    @Test
    void testBoundedLoadIsRespected() {
        Node nodeA = new Node("NodeA");
        Node nodeB = new Node("NodeB");
        Node nodeC = new Node("NodeC");

        router.add(nodeA);
        router.add(nodeB);
        router.add(nodeC);

        // 60 partitions / 3 nodes = 20 * 1.25 loadFactor = 25 -> ceil(25)
        int expectedMaxLoad = (int) Math.ceil(((double) PARTITION_COUNT / 3) * LOAD_FACTOR);

        Map<String, Integer> distribution = router.getLoadDistribution();

        int totalAssigned = 0;
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            int load = entry.getValue();
            assertTrue(load <= expectedMaxLoad,
                    "Node " + entry.getKey() + " exceeded max load! Load: " + load + ", Max: " + expectedMaxLoad);
            totalAssigned += load;
        }

        assertEquals(PARTITION_COUNT, totalAssigned, "All partitions must be assigned");
    }

    @Test
    void testDeterministicRouting() {
        router.add(new Node("NodeA"));
        router.add(new Node("NodeB"));
        router.add(new Node("NodeC"));

        String testKey = "session-999";

        Node firstResult = router.locate(testKey);
        assertNotNull(firstResult);

        for (int i = 0; i < 10; i++) {
            assertEquals(firstResult, router.locate(testKey), "Routing must be deterministic");
        }
    }

    @Test
    void testNodeRemovalRebalancesCluster() {
        Node nodeA = new Node("NodeA");
        Node nodeB = new Node("NodeB");

        router.add(nodeA);
        router.add(nodeB);

        Map<String, Integer> initialDist = router.getLoadDistribution();
        assertTrue(initialDist.get("NodeA") > 0);
        assertTrue(initialDist.get("NodeB") > 0);

        router.remove(nodeA);

        Map<String, Integer> finalDist = router.getLoadDistribution();
        assertNull(finalDist.get("NodeA"));
        assertEquals(PARTITION_COUNT, finalDist.get("NodeB"),
                "NodeB must take over all partitions after NodeA is removed");
    }

    @Test
    void testAddingDuplicateNodeIsIgnored() {
        Node nodeA = new Node("NodeA");

        router.add(nodeA);
        router.add(nodeA);

        Map<String, Integer> dist = router.getLoadDistribution();
        assertEquals(1, dist.size());
        assertEquals(PARTITION_COUNT, dist.get("NodeA"));
    }

    @Test
    void testRemovingNonExistentNodeIsSafe() {
        Node nodeA = new Node("NodeA");
        Node phantomNode = new Node("Phantom");

        router.add(nodeA);
        router.remove(phantomNode);

        assertEquals(PARTITION_COUNT, router.getLoadDistribution().get("NodeA"));
    }

    @Test
    void testConcurrentAddAndRemoveNodes() throws InterruptedException {
        Node nodeA = new Node("NodeA");
        Node nodeB = new Node("NodeB");
        Node nodeC = new Node("NodeC");

        Thread addThread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                router.add(new Node("Node-" + i));
            }
        });

        Thread removeThread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                router.remove(new Node("Node-" + i));
            }
        });

        addThread.start();
        removeThread.start();

        addThread.join();
        removeThread.join();

        router.add(nodeA);
        router.add(nodeB);
        router.add(nodeC);

        Map<String, Integer> distribution = router.getLoadDistribution();
        assertTrue(distribution.containsKey("NodeA"));
        assertTrue(distribution.containsKey("NodeB"));
        assertTrue(distribution.containsKey("NodeC"));
    }

    @Test
    void testConcurrentLocate() throws InterruptedException {
        Node nodeA = new Node("NodeA");
        Node nodeB = new Node("NodeB");
        Node nodeC = new Node("NodeC");

        router.add(nodeA);
        router.add(nodeB);
        router.add(nodeC);

        String testKey = "concurrent-key";

        Thread locateThread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                assertNotNull(router.locate(testKey));
            }
        });

        Thread locateThread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                assertNotNull(router.locate(testKey));
            }
        });

        locateThread1.start();
        locateThread2.start();

        locateThread1.join();
        locateThread2.join();
    }
}
