// src/core/OverlayGraph.java
package core;

import java.util.*;

public class OverlayGraph {
    private final Map<Integer, List<Edge>> adj;

    public OverlayGraph() {
        this.adj = new HashMap<>();
    }

    public void addEdge(int u, int v, double[] weights) {
        if (!adj.containsKey(u)) adj.put(u, new ArrayList<>());
        adj.get(u).add(new Edge(u, v, weights)); 
    }

    public List<Edge> getEdges(int node) {
        return adj.getOrDefault(node, new ArrayList<>());
    }

    public Set<Integer> getNodes() {
        return adj.keySet();
    }
}
