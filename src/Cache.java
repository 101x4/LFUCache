/**
 * Cache interface defining the structure for cache implementations.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K, V> {
    void put(K key, V value);
    V get(K key);
}