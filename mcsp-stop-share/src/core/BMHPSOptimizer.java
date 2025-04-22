// src/core/BMHPSOptimizer.java
package core;

import model.State;
import java.util.*;
import java.util.concurrent.*;

public class BMHPSOptimizer {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static OverlayGraph buildOverlayGraph(Map<Integer, List<Integer>> partitions, Graph graph, int criterionIndex) {
        System.out.println("[BMHPS] Starting parallel overlay graph construction using intra-partition Dijkstra...");

        OverlayGraph overlay = new OverlayGraph();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<OverlayGraph>> futures = new ArrayList<>();

        for (Map.Entry<Integer, List<Integer>> entry : partitions.entrySet()) {
            int partitionId = entry.getKey();
            List<Integer> nodes = entry.getValue();

            Future<OverlayGraph> future = executor.submit(() -> {
                System.out.println("[Thread] Processing partition " + partitionId);
                OverlayGraph localOverlay = new OverlayGraph();
                Set<Integer> boundary = findBoundaryNodes(nodes, graph);
                System.out.println("[Thread] Partition " + partitionId + " has " + boundary.size() + " boundary nodes.");
                if (boundary.size() <= 1) return localOverlay;

                List<Integer> boundaryList = new ArrayList<>(boundary);
                int SAMPLE_BOUNDARY_LIMIT = 10;  
                int limit = Math.min(SAMPLE_BOUNDARY_LIMIT, boundaryList.size());
                for (int i = 0; i < limit; i++) {
                    int src = boundaryList.get(i);
                    Map<Integer, Double> localDistances = intraPartitionDijkstra(graph, src, new HashSet<>(nodes), criterionIndex);
                    for (int j = 0; j < boundaryList.size(); j++) {
                        int dst = boundaryList.get(j);
                        if (src < dst && localDistances.containsKey(dst)) {
                            localOverlay.addEdge(src, dst, new double[]{localDistances.get(dst)});
                            localOverlay.addEdge(dst, src, new double[]{localDistances.get(dst)});
                            
                        }
                    }
                }
                return localOverlay;
            });

            futures.add(future);
        }

        executor.shutdown();
        try {
            for (Future<OverlayGraph> f : futures) {
                OverlayGraph local = f.get();
                for (int u : local.getNodes()) {
                    for (Edge e : local.getEdges(u)) {
                        overlay.addEdge(u, e.to, e.weights);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[BMHPS] Error during parallel processing: " + e.getMessage());
        }

        System.out.println("[BMHPS] Overlay Diagnostics:");
for (int u : overlay.getNodes()) {
    List<Edge> neighbors = overlay.getEdges(u);
    System.out.printf(" - Node %d → %d neighbors\n", u, neighbors.size());
}

        System.out.println("[BMHPS] Finished overlay graph with " + overlay.getNodes().size() + " nodes.");
        return overlay;
    }

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

    private static Map<Integer, Double> intraPartitionDijkstra(Graph graph, int source, Set<Integer> partitionSet, int index) {
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
                if (!partitionSet.contains(v)) continue;  // Only traverse inside partition
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
