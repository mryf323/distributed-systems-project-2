package node;

import file_io.GraphParser;
import message.InitMessage;
import port.EdgePort;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Kompics;
import topology.Graph;
import topology.UndirectedEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class App extends ComponentDefinition {

    Map<String, Component> components = new HashMap<>();

    public App() throws FileNotFoundException {
        createTopology();
    }
    private void createTopology() throws FileNotFoundException {
        Graph graph = GraphParser.getInstance().parseFile(new File("topology.txt"));
        for (String node : graph.nodes) {
            Component component = create(Node.class, new InitMessage(node,
                    graph.getNeighbours(node))
            );
            components.put(node, component);
        }
        for (UndirectedEdge edge : graph.graphEdges) {
            connect(components.get(edge.src).getPositive(EdgePort.class),
                    components.get(edge.dst).getNegative(EdgePort.class),
                    Channel.TWO_WAY);
            connect(components.get(edge.src).getNegative(EdgePort.class),
                    components.get(edge.dst).getPositive(EdgePort.class),
                    Channel.TWO_WAY);
        }
    }
    public static void main(String[] args) throws InterruptedException {

        Kompics.createAndStart(App.class);
        Kompics.waitForTermination();
    }


}
