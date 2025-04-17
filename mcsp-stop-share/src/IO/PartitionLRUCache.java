// src/io/PartitionLRUCache.java
package io;

import java.util.LinkedHashMap;
import java.util.Map;

public class PartitionLRUCache extends LinkedHashMap<Integer, String> {
    private final int CACHE_SIZE;

    public PartitionLRUCache(int size) {
        super(size, 0.75f, true); // accessOrder = true for LRU
        this.CACHE_SIZE = size;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > CACHE_SIZE;
    }

    public void putPartition(int id, String data) {
        this.put(id, data);
    }

    public String getPartition(int id) {
        return this.get(id);
    }

    public boolean containsPartition(int id) {
        return this.containsKey(id);
    }
}
