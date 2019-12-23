package node;

import message.CountLeafResponse;

import java.util.*;

public class CountLeavesState {
    private Map<DirectedEdge, CountLeafResponse> responses = new HashMap<>();
    public void addResponse(DirectedEdge edge, CountLeafResponse response) {
        responses.put(edge, response);
    }

    public Collection<CountLeafResponse> getResponses() {
        return responses.values();
    }
    public CountLeafResponse getResponseOfChild(DirectedEdge edge){
        return responses.get(edge);
    }


}
