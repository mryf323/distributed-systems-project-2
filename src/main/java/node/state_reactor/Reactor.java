package node.state_reactor;

import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Reactor {

    public Reactor() {
    }


    private List<QueuedMessage> messageList = new ArrayList<>();
    private List<StateMutation> occurred = new ArrayList<>();

    public void afterStateChanged(StateMutation e) {
        occurred.add(e);
    }


    public <T extends KompicsEvent> void queue(T message, Handler<T> handler, StateMutation... onEvents) {
        messageList.add(new QueuedMessage<>(message, handler, asList(onEvents)));
    }

    public void react() {
        ArrayList<StateMutation> recentOccurred = new ArrayList<>(this.occurred);
        this.occurred.clear();
        for (StateMutation e : recentOccurred) {
            List<QueuedMessage> toDeliver = messageList.stream()
                    .filter(m -> m.isSubscribed(e)).collect(Collectors.toList());
            messageList.removeAll(toDeliver);
            toDeliver.forEach(QueuedMessage::deliver);
        }


    }
}
