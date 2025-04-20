package core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import core.Edge;
import core.Graph;
import core.MSTBuilder;
import core.EulerTour;
import io.PartitionGenerator;
import io.PartitionLoader;


public class Main {
    public static void main(String[] args) {
        // === CONFIGURATION ===
        int numCriteria = 3; // Number of weight dimensions (criteria)
        int maxMemoryPartitions = 5;
        int partitionSize = 4; // Number of nodes per partition (Euler tour based)

        String datasetName = args[0].replace(".txt", "");
        String datasetPath = "data/" + args[0];
        int source = Integer.parseInt(args[1]);
        int target = Integer.parseInt(args[2]);

        // === LOAD GRAPH FROM FILE ===
        Graph g;
        try {
            g = GraphLoader.loadGraphFromFile(datasetPath, numCriteria);
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return;
        }

        System.out.println("Graph loaded with " + g.getNumVertices() + " vertices.");

        // === RESULT HEADERS ===
        String[] algoFiles = {"ohp", "mhp", "bmhp", "bmhps"};
        for (String algo : algoFiles) {
            try {
                Files.write(Paths.get("results/" + algo + "_results.csv"),
                    "dataset,query_time_ms,disk_reads,cache_hits\n".getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error writing result header for " + algo);
            }
        }

        // === RUN OHP ===
        System.out.println("\nRunning One-Hop Strategy (OHP)...");
        OHPAlgorithm ohp = new OHPAlgorithm(g, numCriteria, source, target);
        long startOHP = System.currentTimeMillis();
        ohp.run();
        long endOHP = System.currentTimeMillis();
        logResult("ohp", datasetName, endOHP - startOHP, ohp.getDiskReads(), ohp.getCacheHits());

        // === RUN MHP ===
        System.out.println("\nRunning Multi-Hop Strategy (MHP)...");
        MHPAlgorithm mhp = new MHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
        long startMHP = System.currentTimeMillis();
        mhp.run();
        long endMHP = System.currentTimeMillis();
        logResult("mhp", datasetName, endMHP - startMHP, mhp.getDiskReads(), mhp.getCacheHits());

        // === RUN BMHP ===
        System.out.println("\nRunning Bidirectional Multi-Hop Strategy (BMHP)...");
        BMHPAlgorithm bmhp = new BMHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
        long startBMHP = System.currentTimeMillis();
        bmhp.run();
        long endBMHP = System.currentTimeMillis();
        logResult("bmhp", datasetName, endBMHP - startBMHP, bmhp.getDiskReads(), bmhp.getCacheHits());

        // === Generate Real Partitions from MST + Euler Tour ===
        List<Edge> mstEdges = MSTBuilder.buildMST(g);
        List<Integer> tour = EulerTour.generateTour(mstEdges, g.getNumVertices());
        PartitionGenerator.writePartitions(tour, partitionSize, "partitions");

        // === Load Partitions and Run BMHPS ===
        System.out.println("\nRunning Shortcut Overlay Optimization (BMHPS)...");
        Map<Integer, List<Integer>> realPartitions = PartitionLoader.loadFromFolder("partitions");

        long startBMHPS = System.currentTimeMillis();
        OverlayGraph overlay = BMHPSOptimizer.buildOverlayGraph(realPartitions, g, 0);
        long endBMHPS = System.currentTimeMillis();

        System.out.println("Overlay Graph Shortcuts:");
        int shortcutCount = 0;
        for (int u : overlay.getNodes()) {
            for (Edge e : overlay.getEdges(u)) {
                if (u < e.to) {
                    shortcutCount++;
                    System.out.println(u + " <-> " + e.to + " weight=" + e.weights[0]);
                }
            }
        }

        // Log BMHPS results
        logResult("bmhps", datasetName, endBMHPS - startBMHPS, overlay.getNodes().size(), shortcutCount);
    }

    private static void logResult(String algo, String dataset, long time, int io, int cache) {
        String line = String.format("%s,%d,%d,%d\n", dataset, time, io, cache);
        try {
            Files.write(Paths.get("results/" + algo + "_results.csv"),
                        line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing results for " + algo + ": " + e.getMessage());
        }
    }
}
