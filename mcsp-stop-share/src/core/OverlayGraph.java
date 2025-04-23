package core;

import java.util.*;

public class OverlayGraph {
    private Map<Integer, List<Edge>> adj; // Adjacency list for the overlay graph

    public OverlayGraph() {
        adj = new HashMap<>();
    }

    // Adds an edge from node u to node v with the given weights
    public void addEdge(int u, int v, double[] weights) {
        adj.computeIfAbsent(u, k -> new ArrayList<>()).add(new Edge(u, v, weights));
    }

    // Returns list of edges from node u
    public List<Edge> getEdges(int u) {
        return adj.getOrDefault(u, new ArrayList<>());
    }

    // Returns set of all nodes in the overlay graph
    public Set<Integer> getNodes() {
        return adj.keySet();
    }

    // Checks if an edge exists from u to v
    public boolean hasEdge(int u, int v) {
        if (!adj.containsKey(u)) return false;
        for (Edge e : adj.get(u)) {
            if (e.to == v) return true;
        }
        return false;
    }

    // Returns the weights of the edge from u to v, or null if not present
    public double[] getWeights(int u, int v) {
        if (!adj.containsKey(u)) return null;
        for (Edge e : adj.get(u)) {
            if (e.to == v) return e.weights;
        }
        return null;
    }
}
