// src/io/PartitionLRUCache.java
package io;

import java.util.LinkedHashMap;
import java.util.Map;

// LRU cache for storing partition data using LinkedHashMap
public class PartitionLRUCache extends LinkedHashMap<Integer, String> {
    private final int CACHE_SIZE; // Maximum number of entries allowed in cache

    public PartitionLRUCache(int size) {
        super(size, 0.75f, true); // true = accessOrder, enabling LRU eviction
        this.CACHE_SIZE = size;
    }

    // Automatically evict the least recently used entry if size exceeds cache limit
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > CACHE_SIZE;
    }

    // Adds a partition entry to the cache
    public void putPartition(int id, String data) {
        this.put(id, data);
    }

    // Retrieves partition data by ID
    public String getPartition(int id) {
        return this.get(id);
    }

    // Checks if the partition is already in cache
    public boolean containsPartition(int id) {
        return this.containsKey(id);
    }
}
