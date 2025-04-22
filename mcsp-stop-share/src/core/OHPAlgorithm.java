// src/core/OHPAlgorithm.java
package core;

import model.State;
import java.util.*;

public class OHPAlgorithm {

    private final Graph graph;
    private final int numCriteria;
    private final int source;
    private final int target;
    private final int maxMemoryPartitions;

    private int diskReads = 0;
    private int cacheHits = 0;

    public OHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
    }

    public int getDiskReads() {
        return diskReads;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public void run() {
        PriorityQueue<State>[] queues = new PriorityQueue[numCriteria];
        Set<Integer>[] visited = new Set[numCriteria];
        for (int i = 0; i < numCriteria; i++) {
            queues[i] = new PriorityQueue<>();
            visited[i] = new HashSet<>();
            queues[i].offer(new State(source, 0));
        }

        Set<Integer> inMemory = new LinkedHashSet<>();  // LRU simulation
        Set<Integer> sharedAccess = new HashSet<>();
        Map<Integer, Set<Integer>> partitionUsers = new HashMap<>();

        boolean[] done = new boolean[numCriteria];

        while (true) {
            boolean allDone = true;

            for (int i = 0; i < numCriteria; i++) {
                if (done[i]) continue;
                allDone = false;

                PriorityQueue<State> queue = queues[i];
                if (!queue.isEmpty()) {
                    State curr = queue.poll();
                    int v = curr.vertex;

                    if (visited[i].contains(v)) continue;
                    visited[i].add(v);

                    int partitionId = v / 200; // Use same partitioning as in PartitionGenerator

                    // Simulate memory access
                    if (!inMemory.contains(partitionId)) {
                        diskReads++;
                        inMemory.add(partitionId);
                        if (inMemory.size() > maxMemoryPartitions) {
                            Iterator<Integer> it = inMemory.iterator();
                            it.next();
                            it.remove();
                        }
                    } else {
                        cacheHits++;
                    }

                    // Track shared access
                    partitionUsers.computeIfAbsent(partitionId, k -> new HashSet<>()).add(i);
                    if (partitionUsers.get(partitionId).size() > 1) {
                        sharedAccess.add(partitionId);
                    }

                    if (v == target) {
                        System.out.println("[OHP] Criterion " + i + " reached target.");
                        done[i] = true;
                        continue;
                    }

                    for (Edge e : graph.getEdges(v)) {
                        int neighbor = e.to;
                        if (!visited[i].contains(neighbor)) {
                            queue.offer(new State(neighbor, curr.cost + e.weights[i]));
                        }
                    }
                }
            }

            if (allDone) break;
        }

        System.out.println("Total Partitions Accessed: " + diskReads);
        System.out.println("Shared Partitions (I/O optimization): " + sharedAccess.size());
    }
}
