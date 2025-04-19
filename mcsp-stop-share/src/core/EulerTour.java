// src/core/EulerTour.java
import java.util.*;
import core.Edge;
import core.Graph;

public class EulerTour {
    public static List<Integer> generateTour(List<Edge> mstEdges, int numVertices) {
        Map<Integer, List<Integer>> tree = new HashMap<>();
        for (Edge edge : mstEdges) {
            tree.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge.to);
            tree.computeIfAbsent(edge.to, k -> new ArrayList<>()).add(edge.from);
        }

        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        dfs(0, tree, visited, tour);
        return tour;
    }

    private static void dfs(int node, Map<Integer, List<Integer>> tree, boolean[] visited, List<Integer> tour) {
        visited[node] = true;
        tour.add(node);
        for (int neighbor : tree.getOrDefault(node, Collections.emptyList())) {
            if (!visited[neighbor]) {
                dfs(neighbor, tree, visited, tour);
                tour.add(node);
            }
        }
    }
}
