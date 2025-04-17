// src/core/Edge.java
package core;

public class Edge {
    public int from;
    public int to;
    public double[] weights;

    public Edge(int from, int to, double[] weights) {
        this.from = from;
        this.to = to;
        this.weights = weights;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Edge(").append(from).append(" -> ").append(to).append(") Weights: ");
        for (double w : weights) sb.append(w).append(" ");
        return sb.toString();
    }
}
