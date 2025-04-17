// src/model/State.java
package model;

public class State implements Comparable<State> {
    public int vertex;
    public double cost;

    public State(int vertex, double cost) {
        this.vertex = vertex;
        this.cost = cost;
    }

    @Override
    public int compareTo(State other) {
        return Double.compare(this.cost, other.cost);
    }
}
