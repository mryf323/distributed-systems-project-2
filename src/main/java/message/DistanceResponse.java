package message;

import node.DirectedEdge;

public final class DistanceResponse extends Distance {

    private final long totalDistance;

    public DistanceResponse(DirectedEdge edge, String rootedAt, long totalDistance) {
        super(edge, rootedAt);
        this.totalDistance = totalDistance;
    }

    public long getTotalDistance() {
        return totalDistance;
    }

    @Override
    public String toString() {
        return "DistanceResponse{" +
                "totalDistance=" + totalDistance +
                ", rootedAt='" + rootedAt + '\'' +
                ", edge=" + edge +
                '}';
    }
}
