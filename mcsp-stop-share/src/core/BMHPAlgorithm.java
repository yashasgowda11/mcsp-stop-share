package core;

import model.State;
import java.util.*;

public class BMHPAlgorithm {
    private final Graph graph; // Input graph
    private final int numCriteria; // Number of criteria (e.g., cost dimensions)
    private final int source; // Source vertex
    private final int target; // Target vertex
    private final int maxMemoryPartitions; // Constraint for memory (unused in this class)

    private int diskReads = 0; // Counter for number of disk reads
    private int cacheHits = 0; // Counter for number of cache hits
    private double[] costs; // Stores shortest path cost for each criterion

    public BMHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
        this.costs = new double[numCriteria];
        Arrays.fill(costs, Double.POSITIVE_INFINITY); // Initialize all costs to infinity
    }

    public void run() {
        for (int i = 0; i < numCriteria; i++) {
            // Priority queues for forward and backward Dijkstra
            PriorityQueue<State> forwardPQ = new PriorityQueue<>();
            PriorityQueue<State> backwardPQ = new PriorityQueue<>();

            // Distance maps
            Map<Integer, Double> forwardDist = new HashMap<>();
            Map<Integer, Double> backwardDist = new HashMap<>();

            // Sets to track visited nodes
            Set<Integer> forwardVisited = new HashSet<>();
            Set<Integer> backwardVisited = new HashSet<>();

            // Initialize source and target
            forwardPQ.offer(new State(source, 0));
            backwardPQ.offer(new State(target, 0));
            forwardDist.put(source, 0.0);
            backwardDist.put(target, 0.0);

            double bestCost = Double.POSITIVE_INFINITY;

            // Bidirectional search loop
            while (!forwardPQ.isEmpty() && !backwardPQ.isEmpty()) {
                expandLayer(forwardPQ, forwardDist, forwardVisited, backwardDist, i);
                expandLayer(backwardPQ, backwardDist, backwardVisited, forwardDist, i);

                // Check for overlap between forward and backward searches
                for (int node : forwardVisited) {
                    if (backwardVisited.contains(node)) {
                        double potentialCost = forwardDist.get(node) + backwardDist.get(node);
                        if (potentialCost < bestCost) bestCost = potentialCost;
                    }
                }

                if (bestCost < Double.POSITIVE_INFINITY) break; // Stop early if path is found
            }

            costs[i] = bestCost; // Save best cost for this criterion
        }
    }

    // Expands one layer of Dijkstra search in either direction
    private void expandLayer(PriorityQueue<State> pq, Map<Integer, Double> dist, Set<Integer> visited,
                             Map<Integer, Double> oppositeDist, int index) {
        if (pq.isEmpty()) return;

        State curr = pq.poll();
        int u = curr.vertex;
        double cost = curr.cost;

        if (visited.contains(u)) return;
        visited.add(u);

        for (Edge edge : graph.getEdges(u)) {
            diskReads++; // Simulate a disk read
            int v = edge.to;
            double weight = edge.weights[index];
            double newCost = cost + weight;

            if (!dist.containsKey(v) || newCost < dist.get(v)) {
                dist.put(v, newCost);
                pq.offer(new State(v, newCost));
            } else {
                cacheHits++; // Simulate cache hit if no update
            }
        }
    }

    // Returns final costs after run
    public double[] getCosts() {
        return costs;
    }

    // Returns total disk reads
    public int getDiskReads() {
        return diskReads;
    }

    // Returns total cache hits
    public int getCacheHits() {
        return cacheHits;
    }
}
