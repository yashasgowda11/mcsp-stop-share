// src/io/PartitionLoader.java
package io;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;


public class PartitionLoader {

    public static Map<Integer, List<Integer>> loadFromFolder(String folderPath) {
        Map<Integer, List<Integer>> partitionMap = new HashMap<>();

        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(folderPath), "partition_*.txt");
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                int partitionId = Integer.parseInt(fileName.replace("partition_", "").replace(".txt", ""));

                List<Integer> nodes = new ArrayList<>();
                for (String line : Files.readAllLines(path)) {
                    if (!line.isBlank()) {
                        nodes.add(Integer.parseInt(line.trim()));
                    }
                }
                partitionMap.put(partitionId, nodes);
                System.out.println("[PartitionLoader] Loaded partition " + partitionId + " with " + nodes.size() + " nodes.");
            }
        } catch (IOException e) {
            System.err.println("[PartitionLoader] Error loading partitions: " + e.getMessage());
        }

        return partitionMap;
    }
} 
