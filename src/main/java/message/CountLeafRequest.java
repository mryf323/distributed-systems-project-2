package message;

import node.DirectedEdge;

public class CountLeafRequest extends BaseMessage {

    public CountLeafRequest(DirectedEdge edge) {
        super(edge);
    }

    @Override
    public String toString() {
        return "CountLeafRequest{" +
                "edge=" + edge +
                '}';
    }
}


