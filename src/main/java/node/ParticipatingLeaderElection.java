package node;

import message.Election;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ParticipatingLeaderElection {

    private DirectedEdge parent;
    private String id;
    private long totalDistance;
    private final Set<Election> responses = new HashSet<>();

    public ParticipatingLeaderElection(DirectedEdge parent,
                                       String id,
                                       long totalDistance) {

        this.parent = parent;
        this.id = id;
        this.totalDistance = totalDistance;
    }

    public Set<Election> getResponses() {
        return responses;
    }

    public void changeParticipatingWave(DirectedEdge parent, String id, long totalDistance) {

        this.parent = parent;
        this.id = id;
        this.totalDistance = totalDistance;
        this.responses.clear();
    }

    public Optional<DirectedEdge> getParent() {
        return Optional.ofNullable(parent);
    }

    public String getId() {
        return id;
    }

    public long getTotalDistance() {
        return totalDistance;
    }

    public void addResponse(Election election) {
        responses.add(election);
    }
}
