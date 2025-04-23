package core;

import model.State;
import java.util.*;

public class OHPAlgorithm {
    private final Graph graph;                // Input graph
    private final int numCriteria;            // Number of cost dimensions
    private final int source;                 // Source node
    private final int target;                 // Target node
    private final int maxMemoryPartitions;    // Max partitions (not used here)

    private int diskReads = 0;                // Disk access counter
    private int cacheHits = 0;                // Cache access counter
    private double[] costs;                   // Stores result costs for each criterion

    public OHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
        this.costs = new double[numCriteria];
        Arrays.fill(costs, Double.POSITIVE_INFINITY); // Initialize all costs to infinity
    }

    // Run one-hop Dijkstra for each criterion independently
    public void run() {
        for (int i = 0; i < numCriteria; i++) {
            PriorityQueue<State> pq = new PriorityQueue<>();  // Min-heap for Dijkstra
            Map<Integer, Double> dist = new HashMap<>();       // Distance tracker
            Set<Integer> visited = new HashSet<>();            // Visited set

            pq.offer(new State(source, 0.0));
            dist.put(source, 0.0);

            while (!pq.isEmpty()) {
                State curr = pq.poll();
                if (visited.contains(curr.vertex)) continue;
                visited.add(curr.vertex);

                if (curr.vertex == target) {
                    costs[i] = curr.cost; // Target found
                    break;
                }

                for (Edge edge : graph.getEdges(curr.vertex)) {
                    diskReads++; // Simulate disk access
                    int v = edge.to;
                    double weight = edge.weights[i];
                    double newCost = curr.cost + weight;

                    if (!dist.containsKey(v) || newCost < dist.get(v)) {
                        dist.put(v, newCost);
                        pq.offer(new State(v, newCost));
                    } else {
                        cacheHits++; // No update needed, considered cache access
                    }
                }
            }
        }
    }

    // Getter for result costs
    public double[] getCosts() {
        return costs;
    }

    // Getter for disk reads
    public int getDiskReads() {
        return diskReads;
    }

    // Getter for cache hits
    public int getCacheHits() {
        return cacheHits;
    }
}
