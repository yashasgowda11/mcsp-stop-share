// src/core/Graph.java
package core;

import java.util.*;

public class Graph {
    private final int numVertices;
    private final Map<Integer, List<Edge>> adjList;

    public Graph(int numVertices) {
        this.numVertices = numVertices;
        this.adjList = new HashMap<>();
        for (int i = 0; i < numVertices; i++) {
            adjList.put(i, new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, double[] weights) {
        Edge edge = new Edge(from, to, weights);
        adjList.get(from).add(edge);

        // Add reverse edge for undirected graph
        Edge reverseEdge = new Edge(to, from, weights);
        adjList.get(to).add(reverseEdge);
    }

    public List<Edge> getEdges(int vertex) {
        return adjList.get(vertex);
    }

    public Set<Integer> getVertices() {
        return adjList.keySet();
    }

    public int getNumVertices() {
        return numVertices;
    }

    public void printGraph() {
        for (int v : adjList.keySet()) {
            System.out.println("Vertex " + v + ":");
            for (Edge e : adjList.get(v)) {
                System.out.println("  " + e);
            }
        }
    }
    public void computeWaveWeights(double[] criteriaWeights) {
        for (List<Edge> edges : adjList.values()) {
            for (Edge edge : edges) {
                if (edge.waveWeight == 0) { // To avoid recomputation
                    double sum = 0;
                    for (int i = 0; i < criteriaWeights.length; i++) {
                        sum += criteriaWeights[i] * edge.weights[i];
                    }
                    edge.waveWeight = sum;
                }
            }
        }
    }
}
