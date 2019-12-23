package message;

import node.DirectedEdge;

public class CountLeafResponse extends BaseMessage {

    private final long count;

    public CountLeafResponse(long count, DirectedEdge edge) {
        super(edge);
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "CountLeafResponse{" +
                "count=" + count +
                ", edge=" + edge +
                '}';
    }
}
