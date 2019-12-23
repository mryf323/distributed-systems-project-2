package message;

import node.DirectedEdge;

public final class DistanceRequest extends Distance {

    private final long distance;

    public DistanceRequest(DirectedEdge edge, String rootedAt, long distance) {
        super(edge, rootedAt);
        this.distance = distance;
    }

    public long getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "DistanceRequest{" +
                "distance=" + distance +
                ", edge=" + edge +
                '}';
    }
}
