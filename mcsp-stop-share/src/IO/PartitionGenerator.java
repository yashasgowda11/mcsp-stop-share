// src/io/PartitionGenerator.java
package io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class PartitionGenerator {
    public static void writePartitions(List<Integer> tour, int partitionSize, String outputFolder) {
        try {
            Files.createDirectories(Paths.get(outputFolder));
            int partitionId = 0;
            for (int i = 0; i < tour.size(); i += partitionSize) {
                List<Integer> part = tour.subList(i, Math.min(i + partitionSize, tour.size()));
                Path outFile = Paths.get(outputFolder, "partition_" + partitionId + ".txt");
                Files.write(outFile, () -> part.stream().<CharSequence>map(String::valueOf).iterator());
                partitionId++;
            }
            System.out.println("[PartitionGenerator] Wrote " + partitionId + " partition files.");
        } catch (IOException e) {
            System.err.println("Error writing partitions: " + e.getMessage());
        }
    }
}
