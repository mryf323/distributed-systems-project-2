package node;

import javafx.util.Pair;

import java.util.List;

public class MapReduceState {

    private int childRespond = 0;
    List<Pair<String, Long>> reductionResult;

    public int getChildRespond() {
        return childRespond;
    }

    public void incrementChildRespond() {
        this.childRespond++;
    }

    public List<Pair<String, Long>> getReductionResult() {
        return reductionResult;
    }

    public void addReductionResult(List<Pair<String, Long>> reductionResult) {
        if(this.reductionResult == null)
            this.reductionResult = reductionResult;
        else
            this.reductionResult.addAll(reductionResult);
    }
}
