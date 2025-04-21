// src/io/PartitionGenerator.java
package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class PartitionGenerator {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void writePartitions(List<Integer> tour, int partitionSize, String folder) {
        try {
            Files.createDirectories(Paths.get(folder));
        } catch (IOException e) {
            System.err.println("[PartitionGenerator] Could not create directory: " + e.getMessage());
            return;
        }

        int numPartitions = (int) Math.ceil(tour.size() / (double) partitionSize);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numPartitions; i++) {
            final int partitionId = i;
            futures.add(executor.submit(() -> {
                int start = partitionId * partitionSize;
                int end = Math.min(start + partitionSize, tour.size());
                List<Integer> partition = tour.subList(start, end);

                if (partition.size() < 3) return; // skip tiny partitions

                Path filePath = Paths.get(folder, "partition_" + partitionId + ".txt");
                try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
                    for (int node : partition) {
                        writer.write(node + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("[PartitionGenerator] Failed to write partition " + partitionId + ": " + e.getMessage());
                }
            }));
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("[PartitionGenerator] Error in thread: " + e.getMessage());
            }
        }

        executor.shutdown();
        System.out.println("[PartitionGenerator] Wrote " + numPartitions + " partition files (size: " + partitionSize + ").");
    }
}
