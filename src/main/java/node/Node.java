package node;

import file_io.Partitioner;
import file_io.WordCountWriter;
import javafx.util.Pair;
import message.*;
import node.state_reactor.Reactor;
import node.state_reactor.event.NodeStatusChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import port.EdgePort;
import se.sics.kompics.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Node extends ComponentDefinition {

    public enum Status {
        FIND,
        FOUND,
    }

    private static Logger logger = LoggerFactory.getLogger(Node.class);

    private final Reactor reactor = new Reactor();

    private final String nodeName;
    private final Positive<EdgePort> receive = positive(EdgePort.class);
    private final Negative<EdgePort> send = negative(EdgePort.class);

    private final Set<DirectedEdge> neighbours;
    private final Map<String, RootedDistanceState> rootedDistanceState = new HashMap<>();
    private final NodeStateHolder nodeStateHolder = new NodeStateHolder(reactor);

    private ParticipatingLeaderElection participatingLeaderElection;
    private TreeInformation treeInformation;
    private CountLeavesState countLeavesState = new CountLeavesState();
    private MapReduceState mapReduceState = new MapReduceState();

    private DirectedEdge findRelevantNeighbour(BaseMessage message) {
        return neighbours.stream().filter(e -> e.dst.equals(message.edge.src))
                .findAny().orElseThrow(() -> new RuntimeException("Problem in topology"));
    }

    public Node(InitMessage initMessage) {

        nodeName = initMessage.nodeName;
        this.neighbours = initMessage.neighbours
                .stream().map(e -> new DirectedEdge(e.src, e.dst, e.w, reactor))
                .collect(Collectors.toSet());

        subscribe(startHandler, control);
        subscribe(distanceRequestHandler, receive);
        subscribe(distanceResponseHandler, receive);
        subscribe(electionHandler, receive);
        subscribe(countLeafRequestHandler, receive);
        subscribe(countLeafResponseHandler, receive);
        subscribe(mapReduceRequestHandler, receive);
        subscribe(reduceHandler, receive);
    }

    private Handler<Start> startHandler = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            RootedDistanceState state = new RootedDistanceState(null);
            rootedDistanceState.put(nodeName, state);
            for (DirectedEdge neighbour : neighbours) {
                DistanceRequest request = new DistanceRequest(neighbour, nodeName, neighbour.w);
                trigger(request, send);
            }
            reactor.react();
        }
    };

    private Handler<DistanceRequest> distanceRequestHandler = new Handler<DistanceRequest>() {
        @Override
        public void handle(DistanceRequest request) {

            if (!request.edge.dst.equals(nodeName))
                return;
//            logger.info(" Node {} received {}", nodeName, request);
            DirectedEdge j = findRelevantNeighbour(request);

            RootedDistanceState state =
                    new RootedDistanceState(new DistanceRequest(j, request.getRootedAt(), request.getDistance()));
            rootedDistanceState.put(request.getRootedAt(), state);
            Set<DirectedEdge> children = neighbours.stream().filter(n -> !n.equals(j)).collect(Collectors.toSet());
            if (children.size() == 0)
                trigger(new DistanceResponse(j, request.getRootedAt(), request.getDistance()), send);
            else
                children.forEach(n ->
                        trigger(new DistanceRequest(n, request.getRootedAt(), request.getDistance() + n.w), send)
                );
            reactor.react();
        }
    };

    private Handler<DistanceResponse> distanceResponseHandler = new Handler<DistanceResponse>() {
        @Override
        public void handle(DistanceResponse response) {

            if (!response.edge.dst.equals(nodeName))
                return;
//            logger.info(" Node {} received {}", nodeName, response);
            DirectedEdge j = findRelevantNeighbour(response);

            RootedDistanceState state = rootedDistanceState.get(response.getRootedAt());
            state.add(new DistanceResponse(j, response.getRootedAt(), response.getTotalDistance()));

            if (
                    (state.getOriginalRequest().isPresent() && state.getResponses().size() >= neighbours.size() - 1) ||
                            (!state.getOriginalRequest().isPresent() && state.getResponses().size() >= neighbours.size())
            ) {
                long result = state.getResponses().stream()
                        .map(DistanceResponse::getTotalDistance)
                        .reduce(0L, Long::sum);

                if (state.getOriginalRequest().isPresent()) {
                    result += state.getOriginalRequest().get().getDistance();
                    DistanceResponse distanceResponse = new DistanceResponse(
                            state.getOriginalRequest().get().getEdge(),
                            state.getOriginalRequest().get().getRootedAt(),
                            result
                    );
                    trigger(distanceResponse, send);
//                    logger.info("Node {} calculated total distance {} and replied its parent.", nodeName, distanceResponse);
                } else {
                    nodeStateHolder.foundTotalDistance(result);
                    logger.info("Node {} calculated its own total distance: {}.", nodeName, result);
                    initiateLeaderElection();
                }
            }
            reactor.react();
        }
    };

    private void initiateLeaderElection() {
        participatingLeaderElection = new ParticipatingLeaderElection(null, nodeName, nodeStateHolder.getTotalDistance());
        for (DirectedEdge neighbour : neighbours) {
            Election initiate = new Election(nodeName, nodeStateHolder.getTotalDistance(), neighbour);
            trigger(initiate, send);
        }
    }

    private Handler<Election> electionHandler = new Handler<Election>() {
        @Override
        public void handle(Election election) {

            if (!election.edge.dst.equals(nodeName))
                return;
            logger.info(" Node {} received {}", nodeName, election);
            DirectedEdge j = findRelevantNeighbour(election);

            if (nodeStateHolder.getNodeStatus() == Status.FIND) {

                reactor.queue(election, electionHandler, new NodeStatusChanged(Status.FOUND));

            } else if (
                    election.getTotalDistance() < participatingLeaderElection.getTotalDistance() ||
                            (
                                    election.getTotalDistance() == participatingLeaderElection.getTotalDistance() &&
                                            election.getNodeId().compareTo(participatingLeaderElection.getId()) < 0
                            )
            ) {

                participatingLeaderElection.changeParticipatingWave(j, election.getNodeId(), election.getTotalDistance());
//                logger.info("Node {} changed participating root to {}", nodeName, election.getNodeId());

                Set<DirectedEdge> children = neighbours.stream()
                        .filter(n -> !n.equals(j)).collect(Collectors.toSet());

                if (children.size() == 0) {
                    Election response = new Election(election.getNodeId(), election.getTotalDistance(), j);
                    trigger(response, send);
//                    logger.info("Responding leader election wave: {}", response);

                } else {
                    children.forEach(child ->
                            trigger(new Election(election.getNodeId(), election.getTotalDistance(), child), send)
                    );
                }

            } else if (participatingLeaderElection.getId().equals(election.getNodeId())) {

                participatingLeaderElection.addResponse(election);

                if (
                        participatingLeaderElection.getParent().isPresent() &&
                                participatingLeaderElection.getResponses().size() >= neighbours.size() - 1
                ) {
                    Election response = new Election(
                            participatingLeaderElection.getId(),
                            participatingLeaderElection.getTotalDistance(),
                            participatingLeaderElection.getParent().get()
                    );
                    trigger(response, send);
//                    logger.info("Responding to wave leader election {}", response);
                } else if (
                        !participatingLeaderElection.getParent().isPresent() &&
                                participatingLeaderElection.getResponses().size() == neighbours.size()
                ) {
                    logger.info("Node {} elected as leader.", nodeName);
                    treeInformation = new TreeInformation(null);
                    neighbours.forEach(c -> trigger(new CountLeafRequest(c), send));
                }
            }
            reactor.react();
        }
    };

    private Handler<CountLeafRequest> countLeafRequestHandler = new Handler<CountLeafRequest>() {
        @Override
        public void handle(CountLeafRequest request) {

            if (!request.edge.dst.equals(nodeName))
                return;
            logger.info(" Node {} received {}", nodeName, request);
            DirectedEdge j = findRelevantNeighbour(request);

            treeInformation = new TreeInformation(j);
            Set<DirectedEdge> children = neighbours.stream().filter(n -> !n.equals(j)).collect(Collectors.toSet());

            if (children.size() == 0) {
                treeInformation.setNumberOfSubTreeLeaves(0);
                trigger(new CountLeafResponse(1, j), send);
            } else
                children.forEach(c -> trigger(new CountLeafRequest(c), send));
            reactor.react();
        }
    };

    private Handler<CountLeafResponse> countLeafResponseHandler = new Handler<CountLeafResponse>() {
        @Override
        public void handle(CountLeafResponse response) {

            if (!response.edge.dst.equals(nodeName))
                return;
            logger.info(" Node {} received {}", nodeName, response);
            DirectedEdge j = findRelevantNeighbour(response);


            countLeavesState.addResponse(j, response);

            if (
                    (!treeInformation.getParent().isPresent() &&
                            countLeavesState.getResponses().size() == neighbours.size()) ||
                            (treeInformation.getParent().isPresent() &&
                                    countLeavesState.getResponses().size() >= neighbours.size() - 1)
            ) {
                long result = countLeavesState.getResponses().stream()
                        .map(CountLeafResponse::getCount).reduce(0L, Long::sum);
                if (treeInformation.getParent().isPresent()) {
                    CountLeafResponse report = new CountLeafResponse(result, treeInformation.getParent().get());
                    trigger(report, send);
                    logger.info("Reporting leaf count: {}", report);
                } else {
                    treeInformation.setNumberOfSubTreeLeaves(result);
                    logger.info("Root counted all leaves {}", result);
                    List<List<String>> partitions = new Partitioner()
                            .partition("pluginfile.txt", treeInformation.getNumberOfSubTreeLeaves());
                    dispatch(partitions);
                }

            }
            reactor.react();
        }
    };

    private Handler<MapReduceRequest> mapReduceRequestHandler = new Handler<MapReduceRequest>() {
        @Override
        public void handle(MapReduceRequest mapReduceRequest) {
            if (!mapReduceRequest.edge.dst.equals(nodeName))
                return;
            logger.info(" Node {} received {}", nodeName, mapReduceRequest);

            if (treeInformation.getNumberOfSubTreeLeaves() == 0)
                map(mapReduceRequest);
            else
                dispatch(mapReduceRequest.getPartitions());

        }
    };

    private void map(MapReduceRequest mapReduceRequest) {

        assert mapReduceRequest.getPartitions().size() == 1;

        List<Pair<String, Long>> mapResult = mapReduceRequest.getPartitions().get(0).stream()
                .flatMap(line -> Stream.of(line.split(" ")))
                .map(w -> new Pair<>(w, 1L)).collect(Collectors.toList());

        assert treeInformation.getParent().isPresent();
        trigger(new WordCountPairs(mapResult, treeInformation.getParent().get()), send);
    }

    private void dispatch(List<List<String>> share) {

        assert treeInformation.getParent().isPresent();
        Set<DirectedEdge> children;
        if (treeInformation.getParent().isPresent()) {
            children = neighbours.stream()
                    .filter(n -> !n.equals(treeInformation.getParent().get())).collect(Collectors.toSet());
        } else {
            children = neighbours;
        }
        int lastPartition = 0;
        for (DirectedEdge neighbour : children) {
            CountLeafResponse responseOfChild = countLeavesState.getResponseOfChild(neighbour);
            int numOfPartitions = (int) Math.ceil(
                    (float) share.size() * responseOfChild.getCount() /
                            treeInformation.getNumberOfSubTreeLeaves()
            );
            int partitionEnd = Math.min(share.size(), lastPartition + numOfPartitions);
            MapReduceRequest request = new MapReduceRequest(share.subList(lastPartition, partitionEnd), neighbour);
            trigger(request, send);
            logger.info("Request sent: {}", request);
            lastPartition = partitionEnd;
        }
    }

    private List<Pair<String, Long>> groupByWord(List<Pair<String, Long>> set) {
        return set.stream().collect(groupingBy(Pair::getKey, summingLong(Pair::getValue)))
                .entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Handler<WordCountPairs> reduceHandler = new Handler<WordCountPairs>() {
        @Override
        public void handle(WordCountPairs wordCountPairs) {
            if (!wordCountPairs.edge.dst.equals(nodeName))
                return;
            logger.info(" Node {} received {}", nodeName, wordCountPairs);
            mapReduceState.incrementChildRespond();
            mapReduceState.addReductionResult(groupByWord(wordCountPairs.getWordCountPairs()));
            if ((treeInformation.getParent().isPresent() && mapReduceState.getChildRespond() >= neighbours.size() - 1) ||
                    (!treeInformation.getParent().isPresent() && mapReduceState.getChildRespond() >= neighbours.size())
            )
                if (treeInformation.getParent().isPresent()) {
                    WordCountPairs reduction = new WordCountPairs(mapReduceState.getReductionResult(), treeInformation.getParent().get());
                    trigger(reduction, send);
                    logger.info("Reporting reduction result to parent: {}", reduction);
                } else {
                    logger.info("Root completed reduction, writing result to file...");
                    WordCountWriter.getInstance().writeStatistics(mapReduceState.reductionResult);
                    Kompics.asyncShutdown();
                }
        }
    };

}
