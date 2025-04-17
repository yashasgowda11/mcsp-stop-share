// src/core/OverlayGraph.java
package core;

import java.util.*;

public class OverlayGraph {
    private final Map<Integer, List<Edge>> adj;

    public OverlayGraph() {
        this.adj = new HashMap<>();
    }

    public void addEdge(int from, int to, double weight) {
        Edge e = new Edge(from, to, new double[]{weight}); // 1D weight for shortcut
        adj.computeIfAbsent(from, k -> new ArrayList<>()).add(e);
        adj.computeIfAbsent(to, k -> new ArrayList<>()).add(new Edge(to, from, new double[]{weight}));
    }

    public List<Edge> getEdges(int node) {
        return adj.getOrDefault(node, new ArrayList<>());
    }

    public Set<Integer> getNodes() {
        return adj.keySet();
    }
}
