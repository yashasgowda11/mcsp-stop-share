// src/core/BMHPSOptimizer.java
package core;

import model.State;
import java.util.*;
import java.util.concurrent.*;

public class BMHPSOptimizer {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors(); // Number of threads for parallelism

    // Builds the overlay graph in parallel using intra-partition Dijkstra
    public static OverlayGraph buildOverlayGraph(Map<Integer, List<Integer>> partitions, Graph graph, int numCriteria) {
        System.out.println("[BMHPS] Starting parallel overlay graph construction using intra-partition Dijkstra...");

        OverlayGraph overlay = new OverlayGraph();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT); // Thread pool for parallel tasks
        List<Future<OverlayGraph>> futures = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : partitions.entrySet()) {
            int partitionId = entry.getKey();
            List<Integer> nodes = entry.getValue();

            // Submit task for each partition
            Future<OverlayGraph> future = executor.submit(() -> {
                System.out.println("[Thread] Processing partition " + partitionId);
                OverlayGraph localOverlay = new OverlayGraph();
                Set<Integer> boundary = findBoundaryNodes(nodes, graph); // Identify boundary nodes
                if (boundary.size() <= 1) return localOverlay; // Skip small partitions

                List<Integer> boundaryList = new ArrayList<>(boundary);
                int SAMPLE_LIMIT = Math.min(30, boundaryList.size()); // Limit boundary samples

                for (int i = 0; i < SAMPLE_LIMIT; i++) {
                    int src = boundaryList.get(i);
                    Map<Integer, double[]> allWeights = new HashMap<>();

                    // Run Dijkstra for each criterion
                    for (int c = 0; c < numCriteria; c++) {
                        Map<Integer, Double> dist = dijkstra(graph, src, new HashSet<>(nodes), c);
                        for (Map.Entry<Integer, Double> e : dist.entrySet()) {
                            int dst = e.getKey();
                            allWeights.putIfAbsent(dst, new double[numCriteria]);
                            allWeights.get(dst)[c] = e.getValue(); // Store weight per criterion
                        }
                    }

                    // Add bidirectional edges to local overlay
                    for (Map.Entry<Integer, double[]> e : allWeights.entrySet()) {
                        int dst = e.getKey();
                        if (src < dst) {
                            localOverlay.addEdge(src, dst, e.getValue());
                            localOverlay.addEdge(dst, src, e.getValue());
                        }
                    }
                }

                return localOverlay;
            });

            futures.add(future);
        }

        executor.shutdown(); // No more tasks submitted
        try {
            for (Future<OverlayGraph> f : futures) {
                OverlayGraph local = f.get();
                // Merge local overlays into the final overlay
                for (int u : local.getNodes()) {
                    for (Edge e : local.getEdges(u)) {
                        overlay.addEdge(u, e.to, e.weights);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BMHPS] Error during parallel processing: " + e.getMessage());
        }

        System.out.println("[BMHPS] Finished overlay graph with " + overlay.getNodes().size() + " nodes.");
        return overlay;
    }

    // Finds boundary nodes that connect to nodes outside the partition
    private static Set<Integer> findBoundaryNodes(List<Integer> nodes, Graph graph) {
        Set<Integer> nodeSet = new HashSet<>(nodes);
        Set<Integer> boundary = new HashSet<>();
        for (int node : nodes) {
            for (Edge e : graph.getEdges(node)) {
                if (!nodeSet.contains(e.to)) {
                    boundary.add(node);
                    break;
                }
            }
        }
        return boundary;
    }

    // Dijkstra restricted to nodes within the partition for one cost criterion
    private static Map<Integer, Double> dijkstra(Graph graph, int source, Set<Integer> partitionSet, int index) {
        Map<Integer, Double> dist = new HashMap<>();
        PriorityQueue<State> pq = new PriorityQueue<>();
        dist.put(source, 0.0);
        pq.offer(new State(source, 0));

        while (!pq.isEmpty()) {
            State curr = pq.poll();
            int u = curr.vertex;
            double cost = curr.cost;

            for (Edge e : graph.getEdges(u)) {
                int v = e.to;
                if (!partitionSet.contains(v)) continue; // Skip if not in partition
                double newCost = cost + e.weights[index];
                if (!dist.containsKey(v) || newCost < dist.get(v)) {
                    dist.put(v, newCost);
                    pq.offer(new State(v, newCost));
                }
            }
        }
        return dist;
    }
}
