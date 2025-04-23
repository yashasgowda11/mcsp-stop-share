package core;

import java.io.*;
import java.util.*;

public class MSTBuilder {

    // Builds the Minimum Spanning Tree (MST) from the dataset using Kruskal's algorithm
    public static List<Edge> buildMST(Graph g, int numCriteria, String datasetPath) {
        List<Edge> edges = new ArrayList<>();

        // Read edge data from file
        try (BufferedReader reader = new BufferedReader(new FileReader(datasetPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue; // Skip comments/blank lines

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2 + numCriteria) {
                    System.err.println("[MSTBuilder] Skipping malformed line: " + line);
                    continue;
                }

                int from = Integer.parseInt(parts[0]); // Source node
                int to = Integer.parseInt(parts[1]);   // Destination node
                double[] weights = new double[numCriteria];

                // Parse all weight criteria
                for (int i = 0; i < numCriteria; i++) {
                    weights[i] = Double.parseDouble(parts[2 + i]);
                }

                edges.add(new Edge(from, to, weights)); // Add edge to list
            }
        } catch (IOException e) {
            System.err.println("[MSTBuilder] Error reading file: " + e.getMessage());
        }

        // Sort edges by the first cost criterion (for MST)
        edges.sort(Comparator.comparingDouble(e -> e.weights[0]));

        // Kruskal's algorithm: union-find to build MST
        UnionFind uf = new UnionFind(g.getNumVertices());
        List<Edge> mst = new ArrayList<>();

        for (Edge edge : edges) {
            if (uf.union(edge.from, edge.to)) { // Add edge if it connects disjoint sets
                mst.add(edge);
            }
        }

        System.out.println("[MSTBuilder] Built MST with " + mst.size() + " edges");
        return mst;
    }
}
