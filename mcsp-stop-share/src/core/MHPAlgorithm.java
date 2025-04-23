package core;

import java.util.*;
import model.State;

public class MHPAlgorithm {

    private final Graph graph;                // Input graph
    private final int numCriteria;            // Number of cost dimensions
    private final int source;                 // Source node
    private final int target;                 // Target node
    private final int maxMemoryPartitions;    // Max number of partitions to simulate memory constraint

    private int diskReads = 0;                // Count of disk reads
    private int cacheHits = 0;                // Count of cache hits
    private double[] costs;                   // Cost results per criterion

    public MHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
        this.costs = new double[numCriteria];
        Arrays.fill(this.costs, Double.POSITIVE_INFINITY); // Initialize all costs to infinity
    }

    // Run the shortest path algorithm for all criteria
    public void run() {
        for (int criterion = 0; criterion < numCriteria; criterion++) {
            runSingleCriterion(criterion);
        }
    }

    // Run Dijkstra for a single cost criterion with simulated memory
    private void runSingleCriterion(int index) {
        Set<Integer> inMemory = new HashSet<>();          // Simulated in-memory partition set
        PriorityQueue<State> pq = new PriorityQueue<>();  // Min-heap for Dijkstra
        Map<Integer, Double> dist = new HashMap<>();      // Distance map

        pq.offer(new State(source, 0));
        dist.put(source, 0.0);

        while (!pq.isEmpty()) {
            State curr = pq.poll();
            int u = curr.vertex;

            if (u == target) {
                costs[index] = curr.cost; // Target reached
                return;
            }

            for (Edge e : graph.getEdges(u)) {
                int v = e.to;
                double weight = e.weights[index];
                double newCost = curr.cost + weight;

                if (!dist.containsKey(v) || newCost < dist.get(v)) {
                    dist.put(v, newCost);
                    pq.offer(new State(v, newCost));
                }

                // Track disk reads vs cache hits
                if (inMemory.contains(u)) {
                    cacheHits++; // Already in memory
                } else {
                    diskReads++; // Simulate disk read
                    if (inMemory.size() >= maxMemoryPartitions) {
                        // Evict one element arbitrarily (FIFO-like)
                        Iterator<Integer> it = inMemory.iterator();
                        it.next(); it.remove();
                    }
                    inMemory.add(u); // Add to memory
                }
            }
        }
    }

    // Getter for disk read count
    public int getDiskReads() {
        return diskReads;
    }

    // Getter for cache hit count
    public int getCacheHits() {
        return cacheHits;
    }

    // Getter for final cost values
    public double[] getCosts() {
        return costs;
    }
}
