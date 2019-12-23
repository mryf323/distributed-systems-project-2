package message;

import node.DirectedEdge;

public abstract class Distance extends BaseMessage {

    protected final String rootedAt;

    public Distance(DirectedEdge edge, String rootedAt) {
        super(edge);
        this.rootedAt = rootedAt;
    }

    public String getRootedAt() {
        return rootedAt;
    }
}
