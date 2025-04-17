package core;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // === CONFIGURATION ===
        String datasetPath = "data/central_usa.txt"; // Change this to orkut.txt, etc.
        int numCriteria = 3; // Number of weight dimensions (criteria)
        int source = 0;
        int target = 100; // Change depending on your dataset
        int maxMemoryPartitions = 5;

        // === LOAD GRAPH FROM FILE ===
        Graph g;
        try {
            g = GraphLoader.loadGraphFromFile(datasetPath, numCriteria);
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return;
        }

        System.out.println("Graph loaded with " + g.getNumVertices() + " vertices.");

        // === RUN OHP ===
        System.out.println("\nRunning One-Hop Strategy (OHP)...");
        OHPAlgorithm ohp = new OHPAlgorithm(g, numCriteria, source, target);
        ohp.run();

        // === RUN MHP ===
        System.out.println("\nRunning Multi-Hop Strategy (MHP)...");
        MHPAlgorithm mhp = new MHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
        mhp.run();

        // === RUN BMHP ===
        System.out.println("\nRunning Bidirectional Multi-Hop Strategy (BMHP)...");
        BMHPAlgorithm bmhp = new BMHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
        bmhp.run();

        // === RUN BMHPS (Overlay Shortcut Graph) ===
        System.out.println("\nRunning Shortcut Overlay Optimization (BMHPS)...");
        Map<Integer, List<Integer>> fakePartitions = new HashMap<>();
        // Fake partitions for demo — Replace with real partition metadata
        fakePartitions.put(0, Arrays.asList(0, 1));
        fakePartitions.put(1, Arrays.asList(2, 3));
        fakePartitions.put(2, Arrays.asList(4, 5));

        OverlayGraph overlay = BMHPSOptimizer.buildOverlayGraph(fakePartitions, g, 0);
        System.out.println("Overlay Graph Shortcuts:");
        for (int u : overlay.getNodes()) {
            for (Edge e : overlay.getEdges(u)) {
                if (u < e.to) {
                    System.out.println(u + " <-> " + e.to + " weight=" + e.weights[0]);
                }
            }
        }
    }
}
