package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import core.*;
import io.PartitionGenerator;
import io.PartitionLoader;

public class Main {
    public static void main(String[] args) {

        // Validate arguments
        if (args.length < 4) {
            System.err.println("Usage: java core.Main <dataset.txt> <numCriteria> <maxMemoryPartitions> <partitionSize>");
            return;
        }

        // === Initialize Parameters ===
        String datasetName = args[0].replace(".txt", "");
        String datasetPath = "data/" + args[0];
        int numCriteria = Integer.parseInt(args[1]);
        int maxMemoryPartitions = Integer.parseInt(args[2]);
        int partitionSize = Integer.parseInt(args[3]);
        boolean forceRegen = args.length >= 5 && args[4].equalsIgnoreCase("true");

        // === Load Graph ===
        Graph g;
        try {
            g = GraphLoader.loadGraphFromFile(datasetPath, numCriteria);
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return;
        }

        System.out.println("Graph loaded with " + g.getNumVertices() + " vertices.");

        // === Load or Generate Partitions ===
        String partitionFolder = "partitions/" + datasetName;
        Path testFile = Paths.get(partitionFolder + "/partition_0.txt");
        Map<Integer, List<Integer>> partitions;

        if (!Files.exists(testFile) || forceRegen) {
            System.out.println("[PartitionGenerator] Generating fresh partitions for " + datasetName);
            List<Edge> mstEdges = MSTBuilder.buildMST(g, numCriteria, datasetPath);
            List<Integer> tour = EulerTour.generateTour(mstEdges, g.getNumVertices());
            PartitionGenerator.writePartitions(tour, partitionSize, partitionFolder);
        }
        partitions = PartitionLoader.loadFromFolder(partitionFolder);

        // === Load or Build Overlay Graph ===
        OverlayGraph overlay = new OverlayGraph();
        Path overlayPath = Paths.get(partitionFolder, "overlay.txt");

        if (Files.exists(overlayPath) && !forceRegen) {
            // Load overlay from file
            System.out.println(" Loading existing overlay from overlay.txt...");
            try {
                List<String> lines = Files.readAllLines(overlayPath);
                for (String line : lines) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        int u = Integer.parseInt(parts[0]);
                        int v = Integer.parseInt(parts[1]);
                        double[] weights = new double[parts.length - 2];
                        for (int i = 0; i < weights.length; i++) {
                            weights[i] = Double.parseDouble(parts[i + 2]);
                        }
                        overlay.addEdge(u, v, weights);
                    }
                }
                System.out.println("Overlay loaded with " + overlay.getNodes().size() + " nodes.");
            } catch (IOException e) {
                System.err.println("[Overlay] Failed to read overlay.txt: " + e.getMessage());
                return;
            }
        } else {
            // Build overlay using BMHPSOptimizer
            System.out.println("Building overlay using BMHPSOptimizer...");
            overlay = BMHPSOptimizer.buildOverlayGraph(partitions, g, numCriteria);
            try (BufferedWriter writer = Files.newBufferedWriter(overlayPath)) {
                for (int u : overlay.getNodes()) {
                    for (Edge e : overlay.getEdges(u)) {
                        if (u < e.to) {
                            writer.write(u + " " + e.to);
                            for (double w : e.weights) writer.write(" " + w);
                            writer.newLine();
                        }
                    }
                }
                System.out.println("Overlay saved to " + overlayPath);
            } catch (IOException e) {
                System.err.println("[Overlay] Failed to save overlay.txt: " + e.getMessage());
            }
        }

        // Validate overlay
        if (overlay.getNodes().isEmpty()) {
            System.err.println("Overlay is empty. Cannot proceed with queries.");
            return;
        }

        // === Load or Generate Queries ===
        List<int[]> queries = QueryGenerator.getOrGenerateQueries(datasetName, overlay, g);
        if (queries.isEmpty()) {
            System.err.println("No valid queries available. Terminating.");
            return;
        }

        // === Execute All Strategies for Each Query ===
        for (int[] pair : queries) {
            int source = pair[0], target = pair[1];
            System.out.println("\n=== Query: " + source + " → " + target + " ===");

            // OHP Strategy
            OHPAlgorithm ohp = new OHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            long t1 = System.currentTimeMillis(); ohp.run(); long t2 = System.currentTimeMillis();
            System.out.println("[OHP]   Time       : " + (t2 - t1) + " ms");
            System.out.println("[OHP]   Disk Reads : " + ohp.getDiskReads());
            System.out.println("[OHP]   Cache Hits : " + ohp.getCacheHits());
            System.out.println("[OHP]   Costs      : " + Arrays.toString(ohp.getCosts()));
            logCSV(datasetName, "OHP", source, target, (t2 - t1), ohp.getDiskReads(), ohp.getCacheHits(), ohp.getCosts());

            // MHP Strategy
            MHPAlgorithm mhp = new MHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            t1 = System.currentTimeMillis(); mhp.run(); t2 = System.currentTimeMillis();
            System.out.println("[MHP]   Time       : " + (t2 - t1) + " ms");
            System.out.println("[MHP]   Disk Reads : " + mhp.getDiskReads());
            System.out.println("[MHP]   Cache Hits : " + mhp.getCacheHits());
            System.out.println("[MHP]   Costs      : " + Arrays.toString(mhp.getCosts()));
            logCSV(datasetName, "MHP", source, target, (t2 - t1), mhp.getDiskReads(), mhp.getCacheHits(), mhp.getCosts());

            // BMHP Strategy
            BMHPAlgorithm bmhp = new BMHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            t1 = System.currentTimeMillis(); bmhp.run(); t2 = System.currentTimeMillis();
            System.out.println("[BMHP]  Time       : " + (t2 - t1) + " ms");
            System.out.println("[BMHP]  Disk Reads : " + bmhp.getDiskReads());
            System.out.println("[BMHP]  Cache Hits : " + bmhp.getCacheHits());
            System.out.println("[BMHP]  Costs      : " + Arrays.toString(bmhp.getCosts()));
            logCSV(datasetName, "BMHP", source, target, (t2 - t1), bmhp.getDiskReads(), bmhp.getCacheHits(), bmhp.getCosts());

            // BMHPS Strategy (Overlay-based)
            t1 = System.currentTimeMillis();
            double[] overlayCosts = OverlayQueryRunner.runMultiCriteriaQuery(overlay, source, target, numCriteria);
            t2 = System.currentTimeMillis();
            System.out.println("[BMHPS] Time       : " + (t2 - t1) + " ms");
            System.out.println("[BMHPS] Costs      : " + Arrays.toString(overlayCosts));
            logCSV(datasetName, "BMHPS", source, target, (t2 - t1), overlay.getNodes().size(), overlay.getEdges(source).size(), overlayCosts);
        }
    }

    // Logs performance metrics to a CSV file for comparison
    public static void logCSV(String dataset, String strategy, int src, int tgt, long timeMs, int diskReads, int cacheHits, double[] costs) {
        try {
            Files.createDirectories(Paths.get("results"));
            Path outPath = Paths.get("results", dataset + ".csv");

            if (!Files.exists(outPath)) {
                Files.write(outPath, "dataset,strategy,source,target,time_ms,disk_reads,cache_hits,cost_0,cost_1,cost_2\n".getBytes());
            }

            StringBuilder row = new StringBuilder();
            row.append(dataset).append(",")
               .append(strategy).append(",")
               .append(src).append(",")
               .append(tgt).append(",")
               .append(timeMs).append(",")
               .append(diskReads).append(",")
               .append(cacheHits);

            for (double cost : costs) {
                row.append(",").append(cost == Double.POSITIVE_INFINITY ? "Inf" : String.format("%.2f", cost));
            }

            row.append("\n");
            Files.write(outPath, row.toString().getBytes(), StandardOpenOption.APPEND);

        } catch (IOException e) {
            System.err.println("[logCSV] Failed to write results: " + e.getMessage());
        }
    }
}
