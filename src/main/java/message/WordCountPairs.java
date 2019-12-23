package message;

import javafx.util.Pair;
import node.DirectedEdge;

import java.util.List;
import java.util.Set;

public class WordCountPairs extends BaseMessage {
    private final List<Pair<String, Long>> wordCountPairs;

    public WordCountPairs(List<Pair<String, Long>> wordCountPairs, DirectedEdge edge) {
        super(edge);
        this.wordCountPairs = wordCountPairs;
    }

    public List<Pair<String, Long>> getWordCountPairs() {
        return wordCountPairs;
    }

}
