import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


/*
 these lombok annotations are use for generate
 mutator functions and constructors.
 you can get rid of them and write them manually.
 */
@Data
@AllArgsConstructor
class MemoryBlock {
    private int address;
    private int length;
    // 0 for idle, 1 for busy
    private int flag;
}

@Data
@AllArgsConstructor
class Job {
    private String name;
    // 0 for ready, 1 for over
    private int status;
    private int needMemory;
}

public class DynamicDispatchRAMSimulate {
    // [0, 1]
    private static int getRandom() {
        return getRandom(0, 1);
    }

    // [1, 10]
    private static int getRandomTen() {
        return getRandom(1, 10);
    }

    //[begin, end]
    private static int getRandom(int begin, int end) {
        double segment = 1.0 / (1 + end - begin);
        return begin + (int) (Math.random() / segment);
    }

    // default for
    public static List<MemoryBlock> initMemoryBlocks(int memoryBlockLength, int memoryLength) {
        List<MemoryBlock> memoryBlockList = new ArrayList<MemoryBlock>();
        int initialDenominator = 512;
        for (int i = 0, addressTemp = 0; i < memoryBlockLength; i++) {
            int currentBlockLength = memoryLength / initialDenominator;
            memoryBlockList.add(new MemoryBlock(addressTemp, currentBlockLength, 0));
            addressTemp += currentBlockLength;
            initialDenominator /= 2;
        }
        return memoryBlockList;
    }

    public static List<Job> initJobs(int jobNum) {
        List<Job> jobList = new ArrayList<Job>();

    }

    public static void main(String[] args) {

    }
}
