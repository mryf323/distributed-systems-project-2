package node.state_reactor.event;

import node.Node;
import node.state_reactor.StateMutation;

import java.util.Objects;

public class NodeStatusChanged implements StateMutation {

    private final Node.Status status;

    public NodeStatusChanged(Node.Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeStatusChanged)) return false;
        NodeStatusChanged that = (NodeStatusChanged) o;
        return status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(status);
    }
}
