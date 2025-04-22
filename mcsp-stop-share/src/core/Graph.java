// src/core/Graph.java
package core;

import java.util.*;

public class Graph {
    private final int numVertices;
    private Map<Integer, List<Edge>> adjList;

    public Graph(int numVertices) {
        this.numVertices = numVertices;
        this.adjList = new HashMap<>();
    }

    public void setAdjacencyMap(Map<Integer, List<Edge>> adj) {
        this.adjList = adj;
    }

    public List<Edge> getEdges(int vertex) {
        return adjList.getOrDefault(vertex, Collections.emptyList());
    }

    public Set<Integer> getVertices() {
        return adjList.keySet();
    }

    public int getNumVertices() {
        return numVertices;
    }

    public Set<Integer> getNodes() {
        return adjList.keySet();
    }
}
