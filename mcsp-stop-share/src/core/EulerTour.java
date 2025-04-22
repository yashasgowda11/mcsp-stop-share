package core;

import java.util.*;

public class EulerTour {
    public static List<Integer> generateTour(List<Edge> mstEdges, int numVertices) {
        Map<Integer, List<Integer>> tree = new HashMap<>();
        for (Edge e : mstEdges) {
            tree.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e.to);
            tree.computeIfAbsent(e.to, k -> new ArrayList<>()).add(e.from);
        }

        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        dfs(0, tree, visited, tour);
        return tour;
    }

    private static void dfs(int node, Map<Integer, List<Integer>> tree, boolean[] visited, List<Integer> tour) {
        visited[node] = true;
        tour.add(node);
        for (int neighbor : tree.getOrDefault(node, new ArrayList<>())) {
            if (!visited[neighbor]) {
                dfs(neighbor, tree, visited, tour);
                tour.add(node); // backtrack
            }
        }
    }
}
