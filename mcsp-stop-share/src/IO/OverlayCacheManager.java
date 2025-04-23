// src/io/OverlayCacheManager.java
package io;

import java.io.*;
import java.nio.file.*;
import core.OverlayGraph;

public class OverlayCacheManager {

    // Serializes and saves the overlay graph to disk for reuse
    public static void saveToCache(String datasetName, OverlayGraph overlay) {
        String path = "partitions/" + datasetName + "/overlay.ser";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(overlay); // Serialize overlay graph
            System.out.println("[OverlayCacheManager] Cached overlay graph to " + path);
        } catch (IOException e) {
            System.err.println("[OverlayCacheManager] Error saving overlay cache: " + e.getMessage());
        }
    }

    // Loads a previously cached overlay graph from disk
    public static OverlayGraph loadFromCache(String datasetName) {
        String path = "partitions/" + datasetName + "/overlay.ser";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            System.out.println("[OverlayCacheManager] Loaded overlay graph from cache.");
            return (OverlayGraph) ois.readObject(); // Deserialize overlay graph
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[OverlayCacheManager] Cache not found or invalid: " + e.getMessage());
            return null;
        }
    }

    // Checks whether a cached overlay graph exists for the dataset
    public static boolean cacheExists(String datasetName) {
        return Files.exists(Paths.get("partitions/" + datasetName + "/overlay.ser"));
    }
}
