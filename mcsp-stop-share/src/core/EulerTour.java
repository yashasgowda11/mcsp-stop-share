package core;

import java.util.*;

public class EulerTour {

    // Generates an Euler tour of the MST using DFS traversal
    public static List<Integer> generateTour(List<Edge> mstEdges, int numVertices) {
        Map<Integer, List<Integer>> tree = new HashMap<>();
        
        // Build undirected tree from MST edges
        for (Edge e : mstEdges) {
            tree.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e.to);
            tree.computeIfAbsent(e.to, k -> new ArrayList<>()).add(e.from);
        }

        List<Integer> tour = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];

        // Start DFS from vertex 0
        dfs(0, tree, visited, tour);
        return tour;
    }

    // Depth-First Search to build Euler tour
    private static void dfs(int node, Map<Integer, List<Integer>> tree, boolean[] visited, List<Integer> tour) {
        visited[node] = true;
        tour.add(node); // Visit current node

        for (int neighbor : tree.getOrDefault(node, new ArrayList<>())) {
            if (!visited[neighbor]) {
                dfs(neighbor, tree, visited, tour); // Recurse into neighbor
                tour.add(node); // Add current node again when backtracking
            }
        }
    }
}
