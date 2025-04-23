// src/core/OverlayQueryRunner.java
package core;

import java.util.*;
import model.State;

public class OverlayQueryRunner {

    // Runs Dijkstra's algorithm on the overlay graph for each criterion independently
    public static double[] runMultiCriteriaQuery(OverlayGraph overlay, int source, int target, int numCriteria) {
        double[] result = new double[numCriteria];
        Arrays.fill(result, Double.POSITIVE_INFINITY); // Initialize results with infinity

        // Run Dijkstra for each cost dimension
        for (int i = 0; i < numCriteria; i++) {
            result[i] = dijkstra(overlay, source, target, i);
        }

        return result;
    }

    // Dijkstra's algorithm for a single criterion (by index)
    private static double dijkstra(OverlayGraph overlay, int source, int target, int index) {
        Map<Integer, Double> dist = new HashMap<>();               // Distance map
        PriorityQueue<State> pq = new PriorityQueue<>();           // Min-heap priority queue
        dist.put(source, 0.0);
        pq.offer(new State(source, 0));

        while (!pq.isEmpty()) {
            State curr = pq.poll();
            int u = curr.vertex;
            double cost = curr.cost;

            if (u == target) return cost; // Reached target

            for (Edge e : overlay.getEdges(u)) {
                int v = e.to;
                // Safely retrieve weight for the given criterion index
                double w = e.weights.length > index ? e.weights[index] : Double.POSITIVE_INFINITY;
                double newCost = cost + w;

                // Relaxation step
                if (!dist.containsKey(v) || newCost < dist.get(v)) {
                    dist.put(v, newCost);
                    pq.offer(new State(v, newCost));
                }
            }
        }

        return Double.POSITIVE_INFINITY; // Return infinity if target is unreachable
    }
}
