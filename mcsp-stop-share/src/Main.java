package core;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import core.Edge;
import core.Graph;
import core.OverlayQueryRunner;
import core.OverlayGraph;
import io.PartitionGenerator;
import io.PartitionLoader;
import io.PartitionCacheManager;
import io.OverlayCacheManager;

public class Main {
    public static void main(String[] args) {
        // === CONFIGURATION ===
        int numCriteria = 3;
        int maxMemoryPartitions = 50;
        int partitionSize = 200;

        String datasetName = args[0].replace(".txt", "");
        String datasetPath = "data/" + args[0];
        boolean forceRegen = args.length > 1 && args[1].equals("--regen");

        // === LOAD GRAPH ===
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
                    "dataset,source,target,query_time_ms,disk_reads,cache_hits\n".getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.err.println("Error writing result header for " + algo);
            }
        }

        // === Prepare Partition Folder ===
        String partitionFolder = "partitions/" + datasetName;
        Map<Integer, List<Integer>> realPartitions;
        if (forceRegen || !PartitionCacheManager.cacheExists(datasetName)) {
            System.out.println("\n[PartitionGenerator] Generating fresh partitions for " + datasetName);
            List<Edge> mstEdges = MSTBuilder.buildMST(g);
            List<Integer> tour = EulerTour.generateTour(mstEdges, g.getNumVertices());
            PartitionGenerator.writePartitions(tour, partitionSize, partitionFolder);
            realPartitions = PartitionLoader.loadFromFolder(partitionFolder);
            PartitionCacheManager.saveToCache(datasetName, realPartitions);
        } else {
            System.out.println("\n[PartitionGenerator] Using cached partition map for " + datasetName);
            realPartitions = PartitionCacheManager.loadFromCache(datasetName);
        }

        // === Load or Build Overlay ===
        OverlayGraph overlay;
        if (forceRegen || !OverlayCacheManager.cacheExists(datasetName)) {
            overlay = BMHPSOptimizer.buildOverlayGraph(realPartitions, g, 0);
            OverlayCacheManager.saveToCache(datasetName, overlay);
        } else {
            overlay = OverlayCacheManager.loadFromCache(datasetName);
        }

        // === Load Queries ===
        List<int[]> queries = new ArrayList<>();
        Path queryFile = Paths.get("queries/" + datasetName + "_queries.txt");
        try {
            List<String> lines = Files.readAllLines(queryFile);
            for (String line : lines) {
                if (!line.isBlank()) {
                    String[] parts = line.trim().split("\\s+");
                    queries.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading queries: " + e.getMessage());
            return;
        }

        // === Run All Algorithms for Each Query ===
        for (int[] pair : queries) {
            int source = pair[0], target = pair[1];
            System.out.println("\n=== Query: " + source + " → " + target + " ===");

            // OHP
            OHPAlgorithm ohp = new OHPAlgorithm(g, numCriteria, source, target);
            long startOHP = System.currentTimeMillis();
            ohp.run();
            long endOHP = System.currentTimeMillis();
            logResult("ohp", datasetName, source, target, endOHP - startOHP, ohp.getDiskReads(), ohp.getCacheHits());

            // MHP
            MHPAlgorithm mhp = new MHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            long startMHP = System.currentTimeMillis();
            mhp.run();
            long endMHP = System.currentTimeMillis();
            logResult("mhp", datasetName, source, target, endMHP - startMHP, mhp.getDiskReads(), mhp.getCacheHits());

            // BMHP
            BMHPAlgorithm bmhp = new BMHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            long startBMHP = System.currentTimeMillis();
            bmhp.run();
            long endBMHP = System.currentTimeMillis();
            logResult("bmhp", datasetName, source, target, endBMHP - startBMHP, bmhp.getDiskReads(), bmhp.getCacheHits());

            // BMHPS
            long startBMHPS = System.currentTimeMillis();
            double[] overlayCosts = OverlayQueryRunner.runMultiCriteriaQuery(overlay, source, target, numCriteria);
            long endBMHPS = System.currentTimeMillis();
            logResult("bmhps", datasetName, source, target, endBMHPS - startBMHPS, overlay.getNodes().size(), overlay.getEdges(source).size());

            for (int i = 0; i < overlayCosts.length; i++) {
                System.out.println("[BMHPS] Criterion " + i + " Cost: " + overlayCosts[i]);
            }
        }
    }

    private static void logResult(String algo, String dataset, int source, int target, long time, int io, int cache) {
        String line = String.format("%s,%d,%d,%d,%d,%d\n", dataset, source, target, time, io, cache);
        try {
            Files.write(Paths.get("results/" + algo + "_results.csv"),
                        line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing results for " + algo + ": " + e.getMessage());
        }
    }
}
