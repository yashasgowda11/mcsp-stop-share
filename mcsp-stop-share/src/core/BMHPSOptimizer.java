// src/core/BMHPSOptimizer.java
package core;

import model.State;
import java.util.*;
import java.util.concurrent.*;

public class BMHPSOptimizer {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int BATCH_SIZE = 100; // Number of partitions per thread task

    public static OverlayGraph buildOverlayGraph(Map<Integer, List<Integer>> partitions, Graph graph, int criterionIndex) {
        List<Map.Entry<Integer, List<Integer>>> partitionList = new ArrayList<>(partitions.entrySet());
        List<List<Map.Entry<Integer, List<Integer>>>> batches = createBatches(partitionList, BATCH_SIZE);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<OverlayGraph>> futures = new ArrayList<>();

        for (List<Map.Entry<Integer, List<Integer>>> batch : batches) {
            futures.add(executor.submit(() -> {
                OverlayGraph localOverlay = new OverlayGraph();
                for (Map.Entry<Integer, List<Integer>> entry : batch) {
                    int partitionId = entry.getKey();
                    List<Integer> nodes = entry.getValue();
                    Set<Integer> boundary = findBoundaryNodes(nodes, graph);

                    if (partitionId % 1000 == 0) {
                        System.out.println("[BMHPS] Processed partition " + partitionId + " (Boundary nodes: " + boundary.size() + ")");
                    }
                    System.out.println("Running partition " + partitionId + " on thread " + Thread.currentThread().getName());


                    if (boundary.size() <= 1) continue;

                    List<Integer> sample = new ArrayList<>(boundary);
                    int limit = Math.min(2, sample.size());
                    for (int i = 0; i < limit; i++) {
                        int src = sample.get(i);
                        Map<Integer, Double> shortcut = prunedDijkstra(graph, src, boundary, criterionIndex);
                        for (Map.Entry<Integer, Double> dest : shortcut.entrySet()) {
                            if (src < dest.getKey()) {
                                localOverlay.addEdge(src, dest.getKey(), dest.getValue());
                            }
                        }
                    }
                }
                return localOverlay;
            }));
        }

        OverlayGraph mergedOverlay = new OverlayGraph();
        for (Future<OverlayGraph> future : futures) {
            try {
                OverlayGraph local = future.get();
                for (int u : local.getNodes()) {
                    for (Edge e : local.getEdges(u)) {
                        mergedOverlay.addEdge(u, e.to, e.weights[0]);
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("[BMHPS] Error merging overlay: " + e.getMessage());
            }
        }

        executor.shutdown();
        return mergedOverlay;
    }

    private static List<List<Map.Entry<Integer, List<Integer>>>> createBatches(List<Map.Entry<Integer, List<Integer>>> partitions, int batchSize) {
        List<List<Map.Entry<Integer, List<Integer>>>> batches = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i += batchSize) {
            int end = Math.min(i + batchSize, partitions.size());
            batches.add(partitions.subList(i, end));
        }
        return batches;
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

    private static Map<Integer, Double> prunedDijkstra(Graph graph, int src, Set<Integer> boundary, int index) {
        Map<Integer, Double> dist = new HashMap<>();
        PriorityQueue<State> pq = new PriorityQueue<>();
        Set<Integer> visited = new HashSet<>();

        pq.offer(new State(src, 0));
        dist.put(src, 0.0);

        while (!pq.isEmpty()) {
            State current = pq.poll();
            int v = current.vertex;

            if (visited.contains(v)) continue;
            visited.add(v);

            for (Edge e : graph.getEdges(v)) {
                int neighbor = e.to;
                double weight = e.weights[index];
                double newCost = current.cost + weight;

                if (!dist.containsKey(neighbor) || newCost < dist.get(neighbor)) {
                    dist.put(neighbor, newCost);
                    pq.offer(new State(neighbor, newCost));
                }
            }
        }

        Map<Integer, Double> filtered = new HashMap<>();
        for (int node : boundary) {
            if (node != src && dist.containsKey(node)) {
                filtered.put(node, dist.get(node));
            }
        }

        return filtered;
    }
}