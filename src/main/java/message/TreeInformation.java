package message;

import node.DirectedEdge;

import java.util.Optional;

public class TreeInformation {

    private final DirectedEdge parent;
    private long numberOfSubTreeLeaves;

    public TreeInformation(DirectedEdge parent) {
        this.parent = parent;
    }

    public void setNumberOfSubTreeLeaves(long numberOfSubTreeLeaves) {
        this.numberOfSubTreeLeaves = numberOfSubTreeLeaves;
    }

    public Optional<DirectedEdge> getParent() {
        return Optional.ofNullable(parent);
    }

    public long getNumberOfSubTreeLeaves() {
        return numberOfSubTreeLeaves;
    }
}
