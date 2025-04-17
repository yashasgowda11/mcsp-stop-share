package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class GraphLoader {
    public static Graph loadGraphFromFile(String path, int numCriteria) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        int maxNode = 0;
        List<String[]> parsed = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.trim().split("\\s+");
            parsed.add(parts);
            maxNode = Math.max(maxNode, Math.max(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])));
        }

        Graph g = new Graph(maxNode + 1);  // Automatically handles vertex count
        for (String[] parts : parsed) {
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            double[] weights = new double[numCriteria];
            for (int i = 0; i < numCriteria; i++) {
                weights[i] = Double.parseDouble(parts[2 + i]);
            }
            g.addEdge(from, to, weights);
        }

        return g;
    }
}
