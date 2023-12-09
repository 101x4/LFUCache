public class Main {
    public static void main(String[] args) throws InterruptedException {
        LFUCache<String, String> cache = new LFUCache<>(3, 2000); // Capacity 3 and TTL 2 seconds
        System.out.println("Cache initialized with capacity of 3 and ttl of 2000");

        // Test Case 1: Adding and retrieving entries
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        System.out.println("Test Case 1: Adding and retrieving entries");
        System.out.println("Get key1: " + cache.get("key1")); // Should return "value1"
        System.out.println("Get key2: " + cache.get("key2")); // Should return "value2"
        System.out.println("Get key3: " + cache.get("key3")); // Should return "value3"
        System.out.println("Size: " + cache.size());
        System.out.println();

        // Test Case 2: Checking for expiration
        System.out.println("Test Case 2: Wait for 4 seconds for expiration");
        Thread.sleep(4000); // Wait for 4 seconds
        System.out.println("Get key1 after expiration: " + cache.get("key1")); // Should return null
        System.out.println("Get key2 after expiration: " + cache.get("key2")); // Should return null
        System.out.println("Get key3 after expiration: " + cache.get("key3")); // Should return null
        System.out.println("Size: " + cache.size()); // Should be zero
        System.out.println();

        // Test Case 3: Eviction of least frequently used
        cache.put("key4", "value4");
        Thread.sleep(4000); // Wait for 4 seconds
        cache.put("key5", "value5");
        cache.put("key6", "value6");
        cache.put("key7", "value7");

        System.out.println("Test Case 3: Eviction of least frequently used");
        System.out.println("Get key4: (expired): " + cache.get("key4")); // Should return null

        System.out.println("Get key7: " + cache.get("key7")); // Should return "value7"
        cache.put("key8", "value8");
        System.out.println("Get key5: (evicted): " + cache.get("key5")); // Should return null
        System.out.println("Get key6: " + cache.get("key6")); // Should return "value6"
        System.out.println("Get key8: " + cache.get("key8")); // Should return "value8"
        System.out.println();
    }
}
