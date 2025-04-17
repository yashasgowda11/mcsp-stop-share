// src/core/BMHPAlgorithm.java
package core;

import model.State;

import java.util.*;

public class BMHPAlgorithm {
    private final Graph graph;
    private final int numCriteria;
    private final int source;
    private final int target;
    private final int maxMemoryPartitions;

    public BMHPAlgorithm(Graph graph, int numCriteria, int source, int target, int maxMemoryPartitions) {
        this.graph = graph;
        this.numCriteria = numCriteria;
        this.source = source;
        this.target = target;
        this.maxMemoryPartitions = maxMemoryPartitions;
    }

    public void run() {
        PriorityQueue<State>[] fwdQueues = new PriorityQueue[numCriteria];
        PriorityQueue<State>[] revQueues = new PriorityQueue[numCriteria];
        Set<Integer>[] fwdVisited = new Set[numCriteria];
        Set<Integer>[] revVisited = new Set[numCriteria];
        Map<Integer, Double>[] fwdDist = new Map[numCriteria];
        Map<Integer, Double>[] revDist = new Map[numCriteria];

        int ioHits = 0;
        Set<Integer> inMemory = new LinkedHashSet<>();
        double[] bestCosts = new double[numCriteria];
        Arrays.fill(bestCosts, Double.MAX_VALUE);

        for (int i = 0; i < numCriteria; i++) {
            fwdQueues[i] = new PriorityQueue<>();
            revQueues[i] = new PriorityQueue<>();
            fwdVisited[i] = new HashSet<>();
            revVisited[i] = new HashSet<>();
            fwdDist[i] = new HashMap<>();
            revDist[i] = new HashMap<>();

            fwdQueues[i].offer(new State(source, 0));
            revQueues[i].offer(new State(target, 0));
            fwdDist[i].put(source, 0.0);
            revDist[i].put(target, 0.0);
        }

        boolean[] terminated = new boolean[numCriteria];

        while (true) {
            boolean allDone = true;

            for (int i = 0; i < numCriteria; i++) {
                if (terminated[i]) continue;
                allDone = false;

                double fwdTop = fwdQueues[i].isEmpty() ? Double.MAX_VALUE : fwdQueues[i].peek().cost;
                double revTop = revQueues[i].isEmpty() ? Double.MAX_VALUE : revQueues[i].peek().cost;

                if (fwdTop + revTop > bestCosts[i]) {
                    terminated[i] = true;
                    System.out.println("Criterion " + i + " terminated: bestCost = " + bestCosts[i]);
                    continue;
                }

                // Forward expansion
                if (!fwdQueues[i].isEmpty()) {
                    State state = fwdQueues[i].poll();
                    int v = state.vertex;
                    if (fwdVisited[i].contains(v)) continue;
                    fwdVisited[i].add(v);

                    int partition = v / 2;
                    if (!inMemory.contains(partition)) {
                        inMemory.add(partition);
                        ioHits++;
                        if (inMemory.size() > maxMemoryPartitions) {
                            Iterator<Integer> it = inMemory.iterator();
                            it.next();
                            it.remove();
                        }
                    }

                    for (Edge e : graph.getEdges(v)) {
                        int neighbor = e.to;
                        double newCost = state.cost + e.weights[i];
                        if (!fwdDist[i].containsKey(neighbor) || newCost < fwdDist[i].get(neighbor)) {
                            fwdDist[i].put(neighbor, newCost);
                            fwdQueues[i].offer(new State(neighbor, newCost));
                            if (revDist[i].containsKey(neighbor)) {
                                bestCosts[i] = Math.min(bestCosts[i], newCost + revDist[i].get(neighbor));
                            }
                        }
                    }
                }

                // Reverse expansion
                if (!revQueues[i].isEmpty()) {
                    State state = revQueues[i].poll();
                    int v = state.vertex;
                    if (revVisited[i].contains(v)) continue;
                    revVisited[i].add(v);

                    int partition = v / 2;
                    if (!inMemory.contains(partition)) {
                        inMemory.add(partition);
                        ioHits++;
                        if (inMemory.size() > maxMemoryPartitions) {
                            Iterator<Integer> it = inMemory.iterator();
                            it.next();
                            it.remove();
                        }
                    }

                    for (Edge e : graph.getEdges(v)) {
                        int neighbor = e.to;
                        double newCost = state.cost + e.weights[i];
                        if (!revDist[i].containsKey(neighbor) || newCost < revDist[i].get(neighbor)) {
                            revDist[i].put(neighbor, newCost);
                            revQueues[i].offer(new State(neighbor, newCost));
                            if (fwdDist[i].containsKey(neighbor)) {
                                bestCosts[i] = Math.min(bestCosts[i], newCost + fwdDist[i].get(neighbor));
                            }
                        }
                    }
                }
            }

            if (allDone) break;
        }

        for (int i = 0; i < numCriteria; i++) {
            System.out.println("Criterion " + i + " Best Path Cost: " + bestCosts[i]);
        }

        System.out.println("\n[BMHP] Total I/O Hits: " + ioHits);
    }
}
