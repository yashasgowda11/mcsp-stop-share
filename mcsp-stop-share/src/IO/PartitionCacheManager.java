// src/io/PartitionCacheManager.java
package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PartitionCacheManager {

    // Save partition map to a binary serialized cache file
    public static void saveToCache(String datasetName, Map<Integer, List<Integer>> partitions) {
        String path = "partitions/" + datasetName + "/partition_map.ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(partitions); // Serialize partition map
            System.out.println("[PartitionCacheManager] Cached partition map to " + path);
        } catch (IOException e) {
            System.err.println("[PartitionCacheManager] Error saving cache: " + e.getMessage());
        }
    }

    // Load the partition map from cache
    @SuppressWarnings("unchecked")
    public static Map<Integer, List<Integer>> loadFromCache(String datasetName) {
        String path = "partitions/" + datasetName + "/partition_map.ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            System.out.println("[PartitionCacheManager] Loaded partition map from cache.");
            return (Map<Integer, List<Integer>>) ois.readObject(); // Deserialize partition map
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PartitionCacheManager] Cache not found or invalid: " + e.getMessage());
            return null;
        }
    }

    // Check if the cache file exists for the given dataset
    public static boolean cacheExists(String datasetName) {
        return Files.exists(Paths.get("partitions/" + datasetName + "/partition_map.ser"));
    }
}
