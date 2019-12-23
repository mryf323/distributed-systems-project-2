package file_io;

import topology.Graph;
import topology.UndirectedEdge;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphParser {

    private static GraphParser instance;

    public static GraphParser getInstance() {

        if (instance == null)
            instance = new GraphParser();
        return instance;
    }

    private GraphParser() {
    }

    public Graph parseFile(File resourceFile) throws FileNotFoundException {
        Set<UndirectedEdge> graphEdges = new HashSet<>();
        Scanner scanner = new Scanner(resourceFile);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();

            if (line.split(",").length > 1) {
                int weight = Integer.parseInt(line.split(",")[1]);
                String rel = line.split(",")[0].trim().toUpperCase();
                String src = rel.split("-")[0].trim().toUpperCase();
                String dst = rel.split("-")[1].trim().toUpperCase();
                graphEdges.add(new UndirectedEdge(src, dst, weight));
            }

        }
        Set<String> allNodes = Stream.concat(graphEdges.stream().map(e -> e.dst), graphEdges.stream().map(e -> e.src))
                .collect(Collectors.toSet());

        return new Graph(allNodes, graphEdges);
    }
}
