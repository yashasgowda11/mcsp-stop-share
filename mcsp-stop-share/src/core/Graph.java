// src/core/Graph.java
package core;

import java.util.*;

public class Graph {
    private final int numVertices; // Total number of vertices in the graph
    private Map<Integer, List<Edge>> adjList; // Adjacency list representation of the graph

    public Graph(int numVertices) {
        this.numVertices = numVertices;
        this.adjList = new HashMap<>();
    }

    // Sets the full adjacency list (used when loading graph from data)
    public void setAdjacencyMap(Map<Integer, List<Edge>> adj) {
        this.adjList = adj;
    }

    // Returns list of edges from a given vertex
    public List<Edge> getEdges(int vertex) {
        return adjList.getOrDefault(vertex, Collections.emptyList());
    }

    // Returns set of all vertex IDs in the graph
    public Set<Integer> getVertices() {
        return adjList.keySet();
    }

    // Alias for getVertices(), for semantic clarity
    public int getNumVertices() {
        return numVertices;
    }

    // Another alias for getVertices(), used in overlay methods
    public Set<Integer> getNodes() {
        return adjList.keySet();
    }
}
