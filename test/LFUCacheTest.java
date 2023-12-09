import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LFUCacheTest {
    private LFUCache<String, Integer> cache;

    @BeforeEach
    void setUp() {
        // Initialize the cache with a capacity of 2 items and TTL of 1000 milliseconds (1 second)
        cache = new LFUCache<>(3, 1000);
    }

    @Test
    void testPutAndGet() {
        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);
        assertEquals(1, cache.get("key1"));
        assertEquals(2, cache.get("key2"));
        assertEquals(3, cache.get("key3"));
    }

    @Test
    void testEvictionPolicy() {
        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);

        // Access key1 one more time
        cache.get("key1");

        // Add another element to trigger eviction
        cache.put("key4", 4);

        assertNull(cache.get("key2")); // key2 should still be there
        assertEquals(1, cache.get("key1")); // key2 should be evicted
        assertEquals(3, cache.get("key3")); // key3 should still be there
    }

    @Test
    void testExpiration() throws InterruptedException {
        cache.put("key1", 1);

        // Wait for longer than the TTL
        Thread.sleep(1500);

        assertNull(cache.get("key1")); // key1 should be expired and thus null
    }

    @Test
    void testSize() {
        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);
        assertEquals(3, cache.size());

        cache.put("key4", 4);
        assertEquals(3, cache.size()); // Size should remain 3 after eviction
    }

    @Test
    void testErrorConstructArguments() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LFUCache<>(-2, 200));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LFUCache<>(2, -200));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LFUCache<>(0, 0));
    }
}
