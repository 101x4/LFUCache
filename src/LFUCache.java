import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A thread-safe LFU (Least Frequently Used) cache implementation in Java.
 * This cache stores key-value pairs and automatically evicts the least frequently used items.
 * It also supports time-based expiration of items.
 *
 * @author Bingyi Jiao
 * @version 1.0
 */

class LFUCache<K, V> implements Cache<K, V>{
    private final int capacity; // The maximum number of items the cache can hold
    private final long ttl; // Time to live in milliseconds
    private final Map<K, Node<K, V>> nodeMap; // Stores the cache entries
    private final Map<Integer, LinkedHashSet<Node<K, V>>> frequencyMap; // Stores nodes by frequency
    private final ReentrantLock lock; // Lock for synchronization
    private final ScheduledExecutorService executorService; // Executor service for managing expiration
    private int minFrequency; // Tracks the minimum frequency of nodes in the cache

    /**
     * Constructor for the LFU cache.
     *
     * @param capacity The maximum number of items that the cache can hold.
     * @param ttl      The time to live in milliseconds for each item.
     */
    public LFUCache(int capacity, long ttl) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Cache capacity must be greater than 0.");
        }
        if (ttl <= 0) {
            throw new IllegalArgumentException("TTL must be greater than 0.");
        }
        this.capacity = capacity;
        this.ttl = ttl;
        this.nodeMap = new ConcurrentHashMap<>();
        this.frequencyMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
        this.minFrequency = 0;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        initializeExpirationTask();
    }

    /**
     * Adds a new key-value pair to the cache. If the key already exists, updates its value.
     * If the cache is full, evicts the least frequently used item before adding the new item.
     * If an item with the key exists and has expired, it is replaced with the new value.
     *
     * @param key   The key with which the specified value is to be associated.
     * @param value The value to be associated with the specified key.
     */
    @Override
    public void put(K key, V value) {
        lock.lock();
        try {
            Node<K, V> node = nodeMap.get(key);
            if (node != null) {
                if (isExpired(node)) {
                    removeNode(node);
                } else {
                    node.value = value;
                    updateFrequency(node);
                    node.lastAccessTime = System.currentTimeMillis();
                    return;
                }
            }
            if (nodeMap.size() >= capacity) {
                evictLFUNode();
            }
            Node<K, V> newNode = new Node<>(key, value);
            addNode(newNode);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves the value associated with the given key from the cache, or null if the key doesn't exist or the item has expired.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the key, or null if no such value or the value has expired.
     */
    @Override
    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = nodeMap.get(key);
            if (node == null || isExpired(node)) {
                return null;
            }
            updateFrequency(node);
            node.lastAccessTime = System.currentTimeMillis();
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    private void updateFrequency(Node<K, V> node) {
        lock.lock();
        try {
            frequencyMap.get(node.frequency).remove(node);
            if (node.frequency == minFrequency && frequencyMap.get(node.frequency).isEmpty()) {
                minFrequency++;
            }
            node.frequency++;
            frequencyMap.computeIfAbsent(node.frequency, k -> new LinkedHashSet<>()).add(node);
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        return nodeMap.size();
    }

    private void evictLFUNode() {
        lock.lock();
        try {
            while (nodeMap.size() >= capacity && !frequencyMap.get(minFrequency).isEmpty()) {
                Node<K, V> candidate = frequencyMap.get(minFrequency).iterator().next();
                removeNode(candidate);
            }
        } finally {
            lock.unlock();
        }
    }

    private void addNode(Node<K, V> node) {
        lock.lock();
        try {
            nodeMap.put(node.key, node);
            frequencyMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(node);
            node.lastAccessTime = System.currentTimeMillis();
            minFrequency = 1;
        } finally {
            lock.unlock();
        }
    }

    private void removeNode(Node<K, V> node) {
        lock.lock();
        try {
            frequencyMap.get(node.frequency).remove(node);
            if (frequencyMap.get(node.frequency).isEmpty()) {
                frequencyMap.remove(node.frequency);
                if (node.frequency == minFrequency) {
                    minFrequency = frequencyMap.keySet().stream()
                            .min(Integer::compare)
                            .orElse(minFrequency);
                }
            }
            nodeMap.remove(node.key);
        } finally {
            lock.unlock();
        }
    }

    private boolean isExpired(Node<K, V> node) {
        return (System.currentTimeMillis() - node.lastAccessTime) > ttl;
    }

    /**
     * Initializes the task that periodically checks for and removes expired nodes.
     */
    private void initializeExpirationTask() {
        executorService.scheduleAtFixedRate(this::removeExpiredNodes, ttl, ttl, TimeUnit.MILLISECONDS);
    }

    private void removeExpiredNodes() {
        lock.lock();
        try {
            for (K key : new HashSet<>(nodeMap.keySet())) {
                Node<K, V> node = nodeMap.get(key);
                if (node != null && isExpired(node)) {
                    removeNode(node);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Node class representing each entry in the cache.
     *
     * @param <K> the type of key
     * @param <V> the type of value
     */
    static class Node<K, V> {
        final K key;
        V value;
        int frequency;
        long lastAccessTime;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.frequency = 1;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}


