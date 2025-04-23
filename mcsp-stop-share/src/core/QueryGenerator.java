package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class QueryGenerator {

    // Loads queries from file if present, or generates 10 random source-target pairs from overlay components
    public static List<int[]> getOrGenerateQueries(String datasetName, OverlayGraph overlay, Graph g) {
        List<int[]> queries = new ArrayList<>();
        Path queryFile = Paths.get("queries", datasetName + "_queries.txt");

        // If query file exists, read queries from it
        if (Files.exists(queryFile)) {
            System.out.println("Reading queries from file...");
            try {
                for (String line : Files.readAllLines(queryFile)) {
                    if (!line.isBlank()) {
                        String[] parts = line.trim().split("\\s+");
                        queries.add(new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading queries: " + e.getMessage());
            }
        } else {
            // No file: generate 10 random source-target pairs from connected components
            System.out.println("No query file found. Sampling 10 random queries from overlay components...");

            List<Integer> overlayNodes = new ArrayList<>(overlay.getNodes());
            Map<Integer, Set<Integer>> components = getConnectedComponents(overlay);
            List<Set<Integer>> validComponents = new ArrayList<>();

            // Only use components with at least 2 nodes
            for (Set<Integer> comp : components.values()) {
                if (comp.size() >= 2) validComponents.add(comp);
            }

            Random rand = new Random();
            int attempts = 0;
            while (queries.size() < 10 && attempts < 1000) {
                if (validComponents.isEmpty()) break;

                Set<Integer> chosen = validComponents.get(rand.nextInt(validComponents.size()));
                List<Integer> compList = new ArrayList<>(chosen);
                int u = compList.get(rand.nextInt(compList.size()));
                int v = compList.get(rand.nextInt(compList.size()));
                if (u != v) {
                    queries.add(new int[]{u, v});
                }
                attempts++;
            }

            // Save generated queries to file
            if (!queries.isEmpty()) {
                try {
                    Files.createDirectories(Paths.get("queries"));
                    BufferedWriter writer = Files.newBufferedWriter(queryFile);
                    for (int[] q : queries) {
                        writer.write(q[0] + " " + q[1]);
                        writer.newLine();
                    }
                    writer.close();
                    System.out.println("Saved generated queries to " + queryFile);
                } catch (IOException e) {
                    System.err.println("Error saving queries: " + e.getMessage());
                }
            }
        }

        return queries;
    }

    // Performs BFS to identify connected components in the overlay graph
    private static Map<Integer, Set<Integer>> getConnectedComponents(OverlayGraph overlay) {
        Map<Integer, Set<Integer>> components = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        int compId = 0;

        for (int node : overlay.getNodes()) {
            if (!visited.contains(node)) {
                Set<Integer> component = new HashSet<>();
                Queue<Integer> queue = new LinkedList<>();
                queue.add(node);
                visited.add(node);

                while (!queue.isEmpty()) {
                    int current = queue.poll();
                    component.add(current);
                    for (Edge edge : overlay.getEdges(current)) {
                        int neighbor = edge.to;
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }

                components.put(compId++, component); // Save component with a unique ID
            }
        }

        return components;
    }
}
