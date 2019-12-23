package node;

import node.state_reactor.Reactor;

import java.util.Objects;

public class DirectedEdge {

    public final String src;
    public final String dst;
    public final long w;
    private final Reactor reactor;


    public DirectedEdge(String src, String dst, long w, Reactor reactor) {
        this.src = src;
        this.dst = dst;
        this.w = w;
        this.reactor = reactor;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "src='" + src + '\'' +
                ", dst='" + dst + '\'' +
                ", w=" + w +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DirectedEdge)) return false;
        DirectedEdge that = (DirectedEdge) o;
        return src.equals(that.src) &&
                dst.equals(that.dst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src, dst);
    }


}
