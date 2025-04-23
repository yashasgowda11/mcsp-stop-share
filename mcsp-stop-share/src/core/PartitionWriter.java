// src/core/PartitionWriter.java
import java.io.*;
import java.util.*;

public class PartitionWriter {

    // Writes partitions of the Euler tour to separate files of given size
    public static void writePartitions(List<Integer> eulerTour, int partitionSize, String outputDir) throws IOException {
        int partitionCount = 0;

        // Iterate over Euler tour in steps of partitionSize
        for (int i = 0; i < eulerTour.size(); i += partitionSize) {
            // Extract a sublist for current partition
            List<Integer> partition = eulerTour.subList(i, Math.min(i + partitionSize, eulerTour.size()));

            // Create output file for the partition
            File file = new File(outputDir, "partition_" + partitionCount + ".txt");

            // Write nodes in the partition to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int node : partition) {
                    writer.write(node + "\n");
                }
            }

            partitionCount++; // Increment partition file index
        }
    }
}
