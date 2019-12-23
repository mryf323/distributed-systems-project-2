package node;

import message.DistanceRequest;
import message.DistanceResponse;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class RootedDistanceState {


    private final Set<DistanceResponse> responses = new HashSet<>();
    private final DistanceRequest originalRequest;

    public RootedDistanceState(DistanceRequest originalRequest) {
        this.originalRequest = originalRequest;
    }

    public Set<DistanceResponse> getResponses() {
        return responses;
    }

    public Optional<DistanceRequest> getOriginalRequest() {
        return Optional.ofNullable(originalRequest);
    }

    public boolean add(DistanceResponse response) {
        return responses.add(response);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RootedDistanceState)) return false;
        RootedDistanceState that = (RootedDistanceState) o;
        return originalRequest.getRootedAt().equals(that.originalRequest.getRootedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalRequest.getRootedAt());
    }
}
