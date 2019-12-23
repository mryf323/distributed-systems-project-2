package file_io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Partitioner {

    public List<List<String>> partition(String fileName, long numOfPartitions) {
        try {
            long partitionSize = (long) Math.ceil(
                    Files.lines(Paths.get(fileName)).parallel().count()
                    / (float) numOfPartitions
            );

        AtomicLong atomicInteger = new AtomicLong(0);
        return new ArrayList<>(Files.lines(Paths.get(fileName)).parallel()
                .collect(Collectors.groupingBy(l -> atomicInteger.getAndIncrement() / partitionSize)).values());
    }catch (IOException e){}
        throw new RuntimeException("Problem opening file");
    }

}
