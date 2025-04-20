// src/core/MSTBuilder.java
package core;

import java.util.*;
import core.Edge;
import core.Graph;

public class MSTBuilder {
    public static List<Edge> buildMST(Graph graph) {
        int n = graph.getNumVertices();
        boolean[] visited = new boolean[n];
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingDouble(e -> e.waveWeight));
        List<Edge> mstEdges = new ArrayList<>();

        visited[0] = true;
        pq.addAll(graph.getEdges(0));

        while (!pq.isEmpty()) {
            Edge edge = pq.poll();
            int u = edge.from;
            int v = edge.to;
            if (visited[u] && visited[v]) continue;

            mstEdges.add(edge);
            int next = visited[u] ? v : u;
            visited[next] = true;
            for (Edge e : graph.getEdges(next)) {
                int neighbor = e.from == next ? e.to : e.from;
                if (!visited[neighbor]) {
                    pq.add(e);
                }
            }
        }
        return mstEdges;
    }
}
