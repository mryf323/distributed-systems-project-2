package node.state_reactor;

import se.sics.kompics.Handler;
import se.sics.kompics.KompicsEvent;

import java.util.List;

class QueuedMessage<T extends KompicsEvent> {
        public QueuedMessage(T message, Handler<T> handler, List<StateMutation> onEvents) {
            this.message = message;
            this.handler = handler;
            this.onEvents = onEvents;
        }

        private final T message;
        private final Handler<T> handler;
        private final List<StateMutation> onEvents;

        public boolean isSubscribed(StateMutation event) {
            return onEvents.contains(event);
        }

        public void deliver(){
            handler.handle(message);
        }
    }