// src/core/Main.java
package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import core.*;
import io.PartitionGenerator;
import io.PartitionLoader;

public class Main {
    public static void main(String[] args) {
        int numCriteria = 3;
        int maxMemoryPartitions = 50;
        int partitionSize = 200;

        String datasetName = args[0].replace(".txt", "");
        String datasetPath = "data/" + args[0];

        Graph g;
        try {
            g = GraphLoader.loadGraphFromFile(datasetPath, numCriteria);
        } catch (IOException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return;
        }

        System.out.println("Graph loaded with " + g.getNumVertices() + " vertices.");

        // === Load or generate partitions ===
        String partitionFolder = "partitions/" + datasetName;
        Path testFile = Paths.get(partitionFolder + "/partition_0.txt");
        Map<Integer, List<Integer>> partitions;

        if (!Files.exists(testFile)) {
            System.out.println("[PartitionGenerator] Generating fresh partitions for " + datasetName);
            List<Edge> mstEdges = MSTBuilder.buildMST(g, numCriteria, datasetPath);
            List<Integer> tour = EulerTour.generateTour(mstEdges, g.getNumVertices());
            PartitionGenerator.writePartitions(tour, partitionSize, partitionFolder);
        }
        partitions = PartitionLoader.loadFromFolder(partitionFolder);

        // === Build or load overlay ===
        OverlayGraph overlay = new OverlayGraph();
        Path overlayPath = Paths.get(partitionFolder, "overlay.txt");

        if (Files.exists(overlayPath)) {
            System.out.println("📂 Loading existing overlay from overlay.txt...");
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
                System.out.println("✅ Overlay loaded with " + overlay.getNodes().size() + " nodes.");
            } catch (IOException e) {
                System.err.println("[Overlay] Failed to read overlay.txt: " + e.getMessage());
                return;
            }
        } else {
            System.out.println("🛠️ Building overlay using BMHPSOptimizer...");
            overlay = BMHPSOptimizer.buildOverlayGraph(partitions, g, 0);
            try (BufferedWriter writer = Files.newBufferedWriter(overlayPath)) {
                for (int u : overlay.getNodes()) {
                    for (Edge e : overlay.getEdges(u)) {
                        if (u < e.to) {
                            writer.write(u + " " + e.to);
                            for (double w : e.weights) {
                                writer.write(" " + w);
                            }
                            writer.write("\n");
                        }
                    }
                }
                System.out.println("✅ Overlay saved to " + overlayPath);
            } catch (IOException e) {
                System.err.println("[Overlay] Failed to save overlay.txt: " + e.getMessage());
            }
        }

        if (overlay.getNodes().isEmpty()) {
            System.err.println("❌ Overlay is empty. Cannot proceed with queries.");
            return;
        }

// === Load or generate queries ===
List<int[]> queries = new ArrayList<>();
Path queryFile = Paths.get("queries/" + datasetName + "_queries.txt");

if (Files.exists(queryFile)) {
    System.out.println("📄 Reading queries from file...");
    try {
        for (String line : Files.readAllLines(queryFile)) {
            if (!line.isBlank()) {
                String[] parts = line.trim().split("\\s+");
                queries.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading queries: " + e.getMessage());
        return;
    }
} else {
    System.out.println("No query file found. Sampling 10 random queries");

    List<int[]> overlayQueries = new ArrayList<>();
    List<Integer> overlayNodes = new ArrayList<>(overlay.getNodes());
    Random rand = new Random();
    Set<Integer> visited = new HashSet<>();
    
    // Function to get connected component using BFS
    Map<Integer, Set<Integer>> components = new HashMap<>();
    int compId = 0;
    for (int node : overlayNodes) {
        if (!visited.contains(node)) {
            Queue<Integer> queue = new LinkedList<>();
            Set<Integer> component = new HashSet<>();
            queue.offer(node);
            visited.add(node);
    
            while (!queue.isEmpty()) {
                int curr = queue.poll();
                component.add(curr);
                for (Edge e : overlay.getEdges(curr)) {
                    if (!visited.contains(e.to)) {
                        visited.add(e.to);
                        queue.offer(e.to);
                    }
                }
            }
    
            components.put(compId++, component);
        }
    }
    
    // Sample 10 queries
    int attempts = 0;
    while (queries.size() < 10 && attempts < 1000) {
        List<Set<Integer>> validComponents = new ArrayList<>();
        for (Set<Integer> comp : components.values()) {
            if (comp.size() >= 2) validComponents.add(comp);
        }
    
        if (validComponents.isEmpty()) break;
    
        Set<Integer> chosen = validComponents.get(rand.nextInt(validComponents.size()));
        List<Integer> compList = new ArrayList<>(chosen);
        int u = compList.get(rand.nextInt(compList.size()));
        int v = compList.get(rand.nextInt(compList.size()));
        if (u != v) {
            queries.add(new int[]{u, v});
        }
        attempts++;
    }    
}

if (queries.isEmpty()) {
    System.err.println("No valid queries available. Terminating.");
    return;
}


        // === Execute all strategies ===
        for (int[] pair : queries) {
            int source = pair[0], target = pair[1];
            System.out.println("\n=== Query: " + source + " → " + target + " ===");

            OHPAlgorithm ohp = new OHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            long t1 = System.currentTimeMillis(); ohp.run(); long t2 = System.currentTimeMillis();
            System.out.println("[OHP] Time: " + (t2 - t1) + " ms | Disk Reads: " + ohp.getDiskReads() + " | Cache Hits: " + ohp.getCacheHits());
            //logCSV(datasetName, "OHP", source, target, (t2 - t1), ohp.getDiskReads(), ohp.getCacheHits(), ohp.getCosts());

            MHPAlgorithm mhp = new MHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            t1 = System.currentTimeMillis(); mhp.run(); t2 = System.currentTimeMillis();
            System.out.println("[MHP] Time: " + (t2 - t1) + " ms | Disk Reads: " + mhp.getDiskReads() + " | Cache Hits: " + mhp.getCacheHits());
            //logCSV(datasetName, "MHP", source, target, (t2 - t1), mhp.getDiskReads(), mhp.getCacheHits(), mhp.getCosts());

            BMHPAlgorithm bmhp = new BMHPAlgorithm(g, numCriteria, source, target, maxMemoryPartitions);
            t1 = System.currentTimeMillis(); bmhp.run(); t2 = System.currentTimeMillis();
            System.out.println("[BMHP] Time: " + (t2 - t1) + " ms | Disk Reads: " + bmhp.getDiskReads() + " | Cache Hits: " + bmhp.getCacheHits());
            //logCSV(datasetName, "BMHP", source, target, (t2 - t1), bmhp.getDiskReads(), bmhp.getCacheHits(), bmhp.getCosts());

            t1 = System.currentTimeMillis();
            double[] overlayCosts = OverlayQueryRunner.runMultiCriteriaQuery(overlay, source, target, numCriteria);
            t2 = System.currentTimeMillis();
            System.out.println("[BMHPS] Time: " + (t2 - t1) + " ms");
            for (int i = 0; i < overlayCosts.length; i++) {
                System.out.println("[BMHPS] Criterion " + i + " Cost: " + overlayCosts[i]);
            }
            //logCSV(datasetName, "BMHPS", source, target, (t2 - t1), overlay.getNodes().size(), overlay.getEdges(source).size(), overlayCosts);
        }
    }

    
}
