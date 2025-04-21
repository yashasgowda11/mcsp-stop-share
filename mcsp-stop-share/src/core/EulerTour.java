// src/core/EulerTour.java
package core;

import java.util.*;
import core.Edge;
import core.Graph;

public class EulerTour {
    public static List<Integer> generateTour(List<Edge> mstEdges, int numVertices) {
        Map<Integer, List<Integer>> tree = new HashMap<>();
        for (Edge e : mstEdges) {
            tree.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e.to);
            tree.computeIfAbsent(e.to, k -> new ArrayList<>()).add(e.from);
        }

        List<Integer> tour = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        dfs(0, tree, visited, tour);
        return tour;
    }

    private static void dfs(int node, Map<Integer, List<Integer>> tree, Set<Integer> visited, List<Integer> tour) {
        visited.add(node);
        tour.add(node);
        for (int neighbor : tree.getOrDefault(node, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, tree, visited, tour);
                tour.add(node); // backtrack
            }
        }
    }
}