// src/core/Edge.java
package core;

public class Edge {
    public int from;               // Source vertex
    public int to;                 // Destination vertex
    public double[] weights;      // Multi-criteria weights for the edge
    public double waveWeight = 0.0; // Optional additional weight (used in some algorithms)

    public Edge(int from, int to, double[] weights) {
        this.from = from;
        this.to = to;
        this.weights = weights;
    }

    // Returns a string representation of the edge and its weights
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edge(").append(from).append(" -> ").append(to).append(") Weights: ");
        for (double w : weights) sb.append(w).append(" ");
        return sb.toString();
    }
}
