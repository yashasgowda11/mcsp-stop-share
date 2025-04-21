// src/io/PartitionCacheManager.java
package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PartitionCacheManager {

    // Save partition map as a binary serialized file
    public static void saveToCache(String datasetName, Map<Integer, List<Integer>> partitions) {
        String path = "partitions/" + datasetName + "/partition_map.ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(partitions);
            System.out.println("[PartitionCacheManager] Cached partition map to " + path);
        } catch (IOException e) {
            System.err.println("[PartitionCacheManager] Error saving cache: " + e.getMessage());
        }
    }

    // Load partition map from cache
    @SuppressWarnings("unchecked")
    public static Map<Integer, List<Integer>> loadFromCache(String datasetName) {
        String path = "partitions/" + datasetName + "/partition_map.ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            System.out.println("[PartitionCacheManager] Loaded partition map from cache.");
            return (Map<Integer, List<Integer>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PartitionCacheManager] Cache not found or invalid: " + e.getMessage());
            return null;
        }
    }

    // Check if cached file exists
    public static boolean cacheExists(String datasetName) {
        return Files.exists(Paths.get("partitions/" + datasetName + "/partition_map.ser"));
    }
} 
