package file_io;

import javafx.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public class WordCountWriter {

    private static WordCountWriter instance;

    private WordCountWriter(){}

    public static WordCountWriter getInstance() {
        if (instance == null)
            instance = new WordCountWriter();
        return instance;
    }

    public void writeStatistics(List<Pair<String, Long>> stats) {
        try {
            FileWriter fileWriter = new FileWriter("result.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            for (Pair<String, Long> stat : stats) {
                printWriter.printf("%s:%d%n", stat.getKey(), stat.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
