package core;

import java.io.*;
import java.util.*;

public class MSTBuilder {

    public static List<Edge> buildMST(Graph g, int numCriteria, String datasetPath) {
        List<Edge> edges = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(datasetPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) continue;

                String[] parts = line.trim().split("\\s+");
                if (parts.length < 2 + numCriteria) {
                    System.err.println("[MSTBuilder] Skipping malformed line: " + line);
                    continue;
                }

                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                double[] weights = new double[numCriteria];
                for (int i = 0; i < numCriteria; i++) {
                    weights[i] = Double.parseDouble(parts[2 + i]);
                }

                edges.add(new Edge(from, to, weights)); 
            }
        } catch (IOException e) {
            System.err.println("[MSTBuilder] Error reading file: " + e.getMessage());
        }

        // Sort edges by the first criterion
        edges.sort(Comparator.comparingDouble(e -> e.weights[0]));

        // Kruskal’s algorithm to build MST
        UnionFind uf = new UnionFind(g.getNumVertices());
        List<Edge> mst = new ArrayList<>();

        for (Edge edge : edges) {
            if (uf.union(edge.from, edge.to)) {
                mst.add(edge);
            }
        }

        System.out.println("[MSTBuilder] Built MST with " + mst.size() + " edges");
        return mst;
    }
}
