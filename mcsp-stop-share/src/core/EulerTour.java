// src/core/EulerTour.java
package core;

import java.util.*;
import core.Edge;
import core.Graph;

public class EulerTour {
    public static List<Integer> generateTour(List<Edge> mstEdges, int numVertices) {
        // Build undirected tree from MST edges
        Map<Integer, List<Integer>> tree = new HashMap<>();
        for (Edge e : mstEdges) {
            tree.computeIfAbsent(e.from, k -> new ArrayList<>()).add(e.to);
            tree.computeIfAbsent(e.to, k -> new ArrayList<>()).add(e.from);
        }

        List<Integer> tour = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Deque<int[]> stack = new ArrayDeque<>();

        stack.push(new int[]{0, -1});  // start from node 0, parent -1

        while (!stack.isEmpty()) {
            int[] pair = stack.pop();
            int node = pair[0];
            int parent = pair[1];

            if (visited.contains(node)) {
                // this is a backtrack — record revisit
                tour.add(node);
                continue;
            }

            visited.add(node);
            tour.add(node);

            stack.push(new int[]{node, parent});  // push revisit marker

            List<Integer> neighbors = tree.getOrDefault(node, Collections.emptyList());
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                int neighbor = neighbors.get(i);
                if (neighbor != parent) {
                    stack.push(new int[]{neighbor, node});
                }
            }
        }

        return tour;
    }
}
