// src/core/PartitionWriter.java
import java.io.*;
import java.util.*;

public class PartitionWriter {
    public static void writePartitions(List<Integer> eulerTour, int partitionSize, String outputDir) throws IOException {
        int partitionCount = 0;
        for (int i = 0; i < eulerTour.size(); i += partitionSize) {
            List<Integer> partition = eulerTour.subList(i, Math.min(i + partitionSize, eulerTour.size()));
            File file = new File(outputDir, "partition_" + partitionCount + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (int node : partition) {
                    writer.write(node + "\n");
                }
            }
            partitionCount++;
        }
    }
}
