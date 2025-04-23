// src/core/GraphLoader.java
package core;

import java.io.*;
import java.util.*;

public class GraphLoader {

    // Loads a graph from a text file with given number of criteria per edge
    public static Graph loadGraphFromFile(String path, int numCriteria) throws IOException {
        Map<Integer, List<Edge>> adj = new HashMap<>(); // Adjacency list

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue; // Skip comments and blanks

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2 + numCriteria) continue; // Skip malformed lines

                int from = Integer.parseInt(parts[0]); // Source node
                int to = Integer.parseInt(parts[1]);   // Destination node
                double[] weights = new double[numCriteria];

                // Parse edge weights for all criteria
                for (int i = 0; i < numCriteria; i++) {
                    weights[i] = Double.parseDouble(parts[2 + i]);
                }

                // Add forward edge to adjacency list
                adj.computeIfAbsent(from, k -> new ArrayList<>())
                    .add(new Edge(from, to, weights));
                // Add reverse edge to make graph undirected
                adj.computeIfAbsent(to, k -> new ArrayList<>())
                    .add(new Edge(to, from, weights));
            }
        }

        // Determine number of vertices based on highest node ID
        int maxNodeId = adj.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        Graph graph = new Graph(maxNodeId);
        graph.setAdjacencyMap(adj);

        return graph;
    }
}
