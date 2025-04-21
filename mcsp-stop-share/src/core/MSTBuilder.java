// src/core/MSTBuilder.java
package core;

import java.util.*;
import core.Edge;
import core.Graph;

public class MSTBuilder {
    public static List<Edge> buildMST(Graph graph) {
        List<Edge> mstEdges = new ArrayList<>();
        boolean[] visited = new boolean[graph.getNumVertices()];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weights[0]));
    
        visited[0] = true;
        pq.addAll(graph.getEdges(0));
    
        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            if (visited[edge.to]) continue;
    
            visited[edge.to] = true;
            mstEdges.add(edge);
    
            for (Edge e : graph.getEdges(edge.to)) {
                if (!visited[e.to]) pq.add(e);
            }
        }
    
        return mstEdges;
    }
    
}
