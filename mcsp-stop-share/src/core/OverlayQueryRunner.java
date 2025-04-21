// src/core/OverlayQueryRunner.java
package core;

import model.State;
import java.util.*;

public class OverlayQueryRunner {

    // Run multi-criteria shortest path query over overlay
    public static double[] runMultiCriteriaQuery(OverlayGraph overlay, int source, int target, int numCriteria) {
        double[] resultCosts = new double[numCriteria];
        Arrays.fill(resultCosts, Double.POSITIVE_INFINITY);

        for (int i = 0; i < numCriteria; i++) {
            resultCosts[i] = runQuery(overlay, source, target, i);
        }

        return resultCosts;
    }

    // Run single-criterion query using Dijkstra
    public static double runQuery(OverlayGraph overlay, int source, int target, int criterionIndex) {
        PriorityQueue<State> pq = new PriorityQueue<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Double> dist = new HashMap<>();

        pq.offer(new State(source, 0));
        dist.put(source, 0.0);

        while (!pq.isEmpty()) {
            State current = pq.poll();
            int u = current.vertex;

            if (u == target) {
                return current.cost;
            }

            if (visited.contains(u)) continue;
            visited.add(u);

            for (Edge edge : overlay.getEdges(u)) {
                int v = edge.to;
                double cost = current.cost + edge.weights[criterionIndex];
                if (!dist.containsKey(v) || cost < dist.get(v)) {
                    dist.put(v, cost);
                    pq.offer(new State(v, cost));
                }
            }
        }

        return Double.POSITIVE_INFINITY; // target not reachable
    }
}