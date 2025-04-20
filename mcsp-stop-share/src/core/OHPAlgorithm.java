// src/core/OHPAlgorithm.java
package core;

import model.State;

import java.util.*;

public class OHPAlgorithm {

    private final Graph graph;
    private final int numCriteria;
    private final int source;
    private final int target;
    private int diskReads = 0;
private int cacheHits = 0;

public int getDiskReads() {
    return diskReads;
}

public int getCacheHits() {
    return cacheHits;
}

    public OHPAlgorithm(Graph graph, int numCriteria, int source, int target) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
    }

    public void run() {
        PriorityQueue<State>[] queues = new PriorityQueue[numCriteria];
        Map<Integer, Set<Integer>> partitionsAccessed = new HashMap<>();
        Set<Integer> sharedAccess = new HashSet<>();
        int ioHits = 0;

        for (int i = 0; i < numCriteria; i++) {
            queues[i] = new PriorityQueue<>();
            queues[i].offer(new State(source, 0));
        }

        Set<Integer>[] visited = new Set[numCriteria];
        for (int i = 0; i < numCriteria; i++) visited[i] = new HashSet<>();

        while (true) {
            boolean allEmpty = true;

            // For each criterion
            for (int i = 0; i < numCriteria; i++) {
                PriorityQueue<State> queue = queues[i];
                if (!queue.isEmpty()) {
                    allEmpty = false;
                    State current = queue.poll();

                    if (visited[i].contains(current.vertex)) continue;
                    visited[i].add(current.vertex);

                    // Simulate a partition being accessed
                    int fakePartitionId = current.vertex / 2;  // Assume each 2 nodes share a partition
                    partitionsAccessed.computeIfAbsent(fakePartitionId, k -> new HashSet<>()).add(i);

                    if (current.vertex == target) {
                        System.out.println("Criterion " + i + " reached target.");
                        continue;
                    }

                    for (Edge edge : graph.getEdges(current.vertex)) {
                        int neighbor = edge.to;
                        if (!visited[i].contains(neighbor)) {
                            queue.offer(new State(neighbor, current.cost + edge.weights[i]));
                        }
                    }
                }
            }

            if (allEmpty) break;
        }

        // Count shared I/O hits
        for (Map.Entry<Integer, Set<Integer>> entry : partitionsAccessed.entrySet()) {
            if (entry.getValue().size() > 1) {
                sharedAccess.add(entry.getKey());
            }
        }

        ioHits = partitionsAccessed.size();

        
    diskReads = partitionsAccessed.size();
    cacheHits = sharedAccess.size();

        System.out.println("\nTotal Partitions Accessed: " + ioHits);
        System.out.println("Shared Partitions (I/O optimization): " + sharedAccess.size());
        System.out.println("Partition IDs with Shared Access: " + sharedAccess);
    }
}
