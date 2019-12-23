package message;

import node.DirectedEdge;

import java.util.List;

public class MapReduceRequest extends BaseMessage {

    private final List<List<String>> partitions;
    public MapReduceRequest(List<List<String>> partitions, DirectedEdge edge) {
        super(edge);
        this.partitions = partitions;
    }

    public List<List<String>> getPartitions() {
        return partitions;
    }

    @Override
    public String toString() {
        return "MapReduceRequest{" +
                "|partitions|=" + partitions.size() +
                ", edge=" + edge +
                '}';
    }
}
