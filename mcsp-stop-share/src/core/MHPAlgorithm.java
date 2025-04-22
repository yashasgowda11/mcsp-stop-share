// src/core/MHPAlgorithm.java
package core;

import model.State;

import java.util.*;

public class MHPAlgorithm {
    private final Graph graph;
    private final int numCriteria;
    private final int source;
    private final int target;
    private final int maxMemoryPartitions;
    private int diskReads = 0;
    private int cacheHits = 0;

    public int getDiskReads() {
        return diskReads;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public MHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
    }

    public void run() {
        PriorityQueue<State>[] queues = new PriorityQueue[numCriteria];
        Set<Integer>[] visited = new Set[numCriteria];
        for (int i = 0; i < numCriteria; i++) {
            queues[i] = new PriorityQueue<>();
            visited[i] = new HashSet<>();
            queues[i].offer(new State(source, 0));
        }

        Set<Integer> inMemory = new LinkedHashSet<>(); // Simulate memory
        int ioHits = 0;
        Set<Integer> sharedAccess = new HashSet<>();
        Map<Integer, Set<Integer>> partitionUsers = new HashMap<>();

        while (true) {
            boolean allEmpty = true;

            for (int i = 0; i < numCriteria; i++) {
                PriorityQueue<State> queue = queues[i];
                while (!queue.isEmpty()) {
                    allEmpty = false;
                    State curr = queue.peek();
                    int partitionId = curr.vertex / 2;

                    if (!inMemory.contains(partitionId)) {
                        ioHits++;
                        inMemory.add(partitionId);

                        // Simulate memory eviction
                        if (inMemory.size() > maxMemoryPartitions) {
                            Iterator<Integer> it = inMemory.iterator();
                            int evicted = it.next();
                            it.remove();
                            //System.out.println("Evicting partition: " + evicted);
                        }
                    }

                    // Track which criteria accessed which partitions
                    partitionUsers.computeIfAbsent(partitionId, k -> new HashSet<>()).add(i);
                    if (partitionUsers.get(partitionId).size() > 1) {
                        sharedAccess.add(partitionId);
                    }

                    State state = queue.poll();
                    if (visited[i].contains(state.vertex)) continue;
                    visited[i].add(state.vertex);

                    if (state.vertex == target) {
                        System.out.println("[MHP] Criterion " + i + " reached target.");
                        break;
                    }

                    for (Edge edge : graph.getEdges(state.vertex)) {
                        if (!visited[i].contains(edge.to)) {
                            queue.offer(new State(edge.to, state.cost + edge.weights[i]));
                        }
                    }

                    // Stop if next vertex needs partition not in memory
                    if (!queue.isEmpty()) {
                        int nextPartition = queue.peek().vertex / 2;
                        if (!inMemory.contains(nextPartition)) {
                            break; // Let others proceed
                        }
                    }
                }
            }

            if (allEmpty) break;
        }

        diskReads = inMemory.size();
        cacheHits = sharedAccess.size();

        System.out.println("\n[MHP] Total Partitions Accessed: " + ioHits);
        System.out.println("[MHP] Shared Partition Access: " + sharedAccess.size());
        //System.out.println("[MHP] Partitions with Shared Access: " + sharedAccess);
    }
}
