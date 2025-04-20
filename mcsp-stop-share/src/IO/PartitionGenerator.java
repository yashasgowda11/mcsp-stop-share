// src/io/PartitionGenerator.java
package io;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class PartitionGenerator {

    public static void writePartitions(List<Integer> tour, int partitionSize, String folderPath) {
        try {
            Files.createDirectories(Paths.get(folderPath));

            int partitionId = 0;
            for (int i = 0; i < tour.size(); i += partitionSize) {
                List<Integer> partition = tour.subList(i, Math.min(i + partitionSize, tour.size()));
                Path filePath = Paths.get(folderPath, "partition_" + partitionId + ".txt");

                List<String> lines = new ArrayList<>();
                for (Integer node : partition) {
                    lines.add(String.valueOf(node));
                }
                Files.write(filePath, lines);
                System.out.println("[PartitionGenerator] Wrote " + filePath);

                partitionId++;
            }
        } catch (IOException e) {
            System.err.println("[PartitionGenerator] Error writing partitions: " + e.getMessage());
        }
    }
} 
