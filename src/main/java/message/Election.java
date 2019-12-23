package message;

import node.DirectedEdge;

public final class Election extends BaseMessage {

    private final String nodeId;
    private final long totalDistance;

    public Election(String nodeId, long totalDistance, DirectedEdge edge) {
        super(edge);
        this.nodeId = nodeId;
        this.totalDistance = totalDistance;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getTotalDistance() {
        return totalDistance;
    }

    @Override
    public String toString() {
        return "Election{" +
                "nodeId='" + nodeId + '\'' +
                ", totalDistance=" + totalDistance +
                ", edge=" + edge +
                '}';
    }
}
