// src/core/GraphLoader.java
package core;

import java.io.*;
import java.util.*;

public class GraphLoader {

    public static Graph loadGraphFromFile(String path, int numCriteria) throws IOException {
        Map<Integer, List<Edge>> adj = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2 + numCriteria) continue;

                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                double[] weights = new double[numCriteria];
                for (int i = 0; i < numCriteria; i++) {
                    weights[i] = Double.parseDouble(parts[2 + i]);
                }

                // Add forward edge
                adj.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(new Edge(from, to, weights));
                // Add reverse edge
                adj.computeIfAbsent(to, k -> new ArrayList<>())
                    .add(new Edge(to, from, weights));
            }
        }

        int maxNodeId = adj.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        Graph graph = new Graph(maxNodeId);
        graph.setAdjacencyMap(adj);

        return graph;
    }
}
