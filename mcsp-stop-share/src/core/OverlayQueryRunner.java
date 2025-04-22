// src/core/OverlayQueryRunner.java
package core;

import java.util.*;
import model.State;

public class OverlayQueryRunner {

    public static double runQuery(OverlayGraph overlay, int source, int target, int index) {
        PriorityQueue<State> pq = new PriorityQueue<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Double> dist = new HashMap<>();

        pq.offer(new State(source, 0));
        dist.put(source, 0.0);

        while (!pq.isEmpty()) {
            State curr = pq.poll();
            int u = curr.vertex;

            if (visited.contains(u)) continue;
            visited.add(u);

            if (u == target) return curr.cost;

            for (Edge edge : overlay.getEdges(u)) {
                int v = edge.to;
                double cost = edge.weights.length > index ? edge.weights[index] : edge.weights[0];
                double newDist = curr.cost + cost;

                if (!dist.containsKey(v) || newDist < dist.get(v)) {
                    dist.put(v, newDist);
                    pq.offer(new State(v, newDist));
                }
            }
        }

        return Double.POSITIVE_INFINITY;
    }

    public static double[] runMultiCriteriaQuery(OverlayGraph overlay, int source, int target, int numCriteria) {
        double[] results = new double[numCriteria];
        for (int i = 0; i < numCriteria; i++) {
            results[i] = runQuery(overlay, source, target, i);
        }
        return results;
    }
} 
