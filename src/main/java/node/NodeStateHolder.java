package node;

import node.state_reactor.Reactor;
import node.state_reactor.event.NodeStatusChanged;

public class NodeStateHolder {

    private Node.Status nodeStatus = Node.Status.FIND;
    private long totalDistance;
    private final Reactor reactor;

    public NodeStateHolder(Reactor reactor) {
        this.reactor = reactor;
    }

    public void foundTotalDistance (long totalDistance) {
        this.totalDistance = totalDistance;
        nodeStatus = Node.Status.FOUND;
        reactor.afterStateChanged(new NodeStatusChanged(Node.Status.FOUND));
    }

    public long getTotalDistance() {
        return totalDistance;
    }

    public Node.Status getNodeStatus() {
        return nodeStatus;
    }
}
