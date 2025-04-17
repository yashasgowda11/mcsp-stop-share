// src/core/BMHPSOptimizer.java
package core;

import model.State;

import java.util.*;

public class BMHPSOptimizer {

    // Main function to build the overlay graph from partitions
    public static OverlayGraph buildOverlayGraph(Map<Integer, List<Integer>> partitions, Graph graph, int criterionIndex) {
        OverlayGraph overlay = new OverlayGraph();

        for (Map.Entry<Integer, List<Integer>> entry : partitions.entrySet()) {
            List<Integer> nodes = entry.getValue();
            Set<Integer> boundary = findBoundaryNodes(nodes, graph);

            for (int src : boundary) {
                Map<Integer, Double> shortcut = prunedDijkstra(graph, src, boundary, criterionIndex);
                for (Map.Entry<Integer, Double> dest : shortcut.entrySet()) {
                    if (src < dest.getKey()) {
                        overlay.addEdge(src, dest.getKey(), dest.getValue());
                    }
                }
            }
        }

        return overlay;
    }

    // Finds boundary nodes of a partition
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

    // Modified Dijkstra's algorithm to return local distances to other boundary nodes
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

        // Only keep distances between boundary nodes
        Map<Integer, Double> filtered = new HashMap<>();
        for (int node : boundary) {
            if (node != src && dist.containsKey(node)) {
                filtered.put(node, dist.get(node));
            }
        }

        return filtered;
    }
}
