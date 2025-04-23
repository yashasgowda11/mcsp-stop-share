// src/io/PartitionLoader.java
package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class PartitionLoader {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors(); // Use all available cores

    // Loads partition files from a folder concurrently and returns a map of partitionId -> list of node IDs
    public static Map<Integer, List<Integer>> loadFromFolder(String folderPath) {
        Map<Integer, List<Integer>> partitionMap = new ConcurrentHashMap<>(); // Thread-safe map
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath), "partition_*.txt")) {
            for (Path path : stream) {
                // Submit a task for each partition file
                futures.add(executor.submit(() -> {
                    String fileName = path.getFileName().toString();
                    int partitionId = Integer.parseInt(fileName.replace("partition_", "").replace(".txt", ""));

                    List<Integer> nodes = new ArrayList<>();
                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                nodes.add(Integer.parseInt(line)); // Add each node to the list
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("[PartitionLoader] Error reading " + fileName + ": " + e.getMessage());
                    }

                    if (nodes.size() >= 3) {
                        partitionMap.put(partitionId, nodes); // Store partition if it has at least 3 nodes
                        /*
                        if (partitionId % 1000 == 0) {
                            System.out.println("[PartitionLoader] Loaded partition " + partitionId + " with " + nodes.size() + " nodes.");
                        }
                        */
                    }
                }));
            }

            // Wait for all threads to finish
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("[PartitionLoader] Thread error: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[PartitionLoader] Failed to list partition files: " + e.getMessage());
        }

        executor.shutdown();
        System.out.println("[PartitionLoader] Loaded " + partitionMap.size() + " partitions from " + folderPath);
        return partitionMap;
    }
}
