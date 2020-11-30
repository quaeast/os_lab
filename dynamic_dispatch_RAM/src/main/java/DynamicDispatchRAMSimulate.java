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
    private int address;
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

    // [begin, end]
    private static int getRandom(int begin, int end) {
        double segment = 1.0 / (1 + end - begin);
        return begin + (int) (Math.random() / segment);
    }

    // default for 10, 1024
    public static List<MemoryBlock> initMemoryBlocks(int memoryBlockLength, int memoryLength) {
        List<MemoryBlock> memoryBlockList = new ArrayList<MemoryBlock>();
        int initialDenominator = 512;
        for (int i = 0, addressTemp = 0; i < memoryBlockLength; i++) {
            int currentBlockLength = memoryLength / initialDenominator;
            memoryBlockList.add(new MemoryBlock(addressTemp, currentBlockLength, 0));
            addressTemp += currentBlockLength;
            initialDenominator /= 2;
        }
        MemoryBlock lastBlock = memoryBlockList.get(memoryBlockList.size() - 1);
        lastBlock.setLength(memoryLength - lastBlock.getAddress() + 1);
        return memoryBlockList;
    }

    // default for 10, 1024
    public static List<Job> initJobs(int jobNum, int memoryLength) {
        List<Job> jobList = new ArrayList<Job>();
        for (int i = 0; i < jobNum; i++) {
            jobList.add(new Job("job " + i, 0,memoryLength/2, -1));
        }
        return jobList;
    }

    // format: address(length)
    public static void showJobList(List<Job> jobList) {
        for (Job i : jobList) {
            System.out.printf("%s: %d(%s), ", i.getName(), i.getAddress(), i.getNeedMemory());
        }
        System.out.println();
    }

    // format: [begin ~ end] for allocated, |begin ~ end| for freed
    public static void showMemoryList(List<MemoryBlock> memoryBlockList) {
        for (MemoryBlock i : memoryBlockList) {
            if (i.getFlag() == 0) {
                System.out.print("|" + i.getAddress() + " ~ " + (i.getAddress() + i.getLength() - 1) + "|");
            } else {
                System.out.print("[" + i.getAddress() + " ~ " + (i.getAddress() + i.getLength() - 1) + "]");
            }
        }
        System.out.println();
    }


    public static boolean allocate(List<MemoryBlock> memoryBlockList, Job job, int allocatePosition) {
        MemoryBlock allocateBlock = memoryBlockList.get(allocatePosition);
        job.setAddress(allocateBlock.getAddress());
        allocateBlock.setFlag(1);
        if (allocateBlock.getLength() == job.getNeedMemory()) {
            return true;
        } else if (allocateBlock.getLength() > job.getNeedMemory()) {
            MemoryBlock remainBlock = new MemoryBlock(
                    allocateBlock.getAddress() + job.getNeedMemory(),
                    allocateBlock.getLength() - job.getNeedMemory(),
                    0);
            allocateBlock.setLength(job.getNeedMemory());
            memoryBlockList.add(allocatePosition + 1, remainBlock);
            return true;
        } else {
            return false;
        }
    }

    public static boolean free(List<MemoryBlock> memoryBlockList, Job job) {
        int position = -1;
        for (int i = 0; i < memoryBlockList.size(); i++) {
            if (job.getAddress() == memoryBlockList.get(i).getAddress()) {
                position = i;
                break;
            }
        }
        if (position == -1) {
            return false;
        }

        job.setStatus(1);

        if (position - 1 >= 0 && memoryBlockList.get(position - 1).getFlag() == 0) {
            MemoryBlock preBlock = memoryBlockList.get(position - 1);
            preBlock.setLength(memoryBlockList.get(position).getLength() + preBlock.getLength());
            memoryBlockList.remove(position);
            position--;
        }
        if (position + 1 < memoryBlockList.size() && memoryBlockList.get(position + 1).getFlag() == 0) {
            MemoryBlock postBlock = memoryBlockList.get(position + 1);
            memoryBlockList.get(position).setLength(memoryBlockList.get(position).getLength() +
                    postBlock.getLength());
            memoryBlockList.remove(position + 1);
        }
        memoryBlockList.get(position).setFlag(0);
        return true;
    }

    public static void running(List<MemoryBlock> memoryBlockList, List<Job> runningJobs, List<Job> overJobs) {
        for (int i = 0; i < runningJobs.size(); ) {
            if (getRandom() == 1) {
                free(memoryBlockList, runningJobs.get(i));
                overJobs.add(runningJobs.get(i));
                runningJobs.remove(i);
            } else {
                i++;
            }
        }
    }

    // FF denote for first fit
    // return cursor, -1 for fail
    public static int FFLocate(List<MemoryBlock> memoryBlockList, Job job, int cursor) {
        int currentCursor = cursor;
        int initialCursor = cursor;
        while (true) {
            MemoryBlock currentBlock = memoryBlockList.get(currentCursor);
            if (currentBlock.getFlag() == 0 && job.getNeedMemory() <= currentBlock.getLength()) {
                return currentCursor;
            } else {
                currentCursor = (currentCursor + 1) % memoryBlockList.size();
            }
            if (currentCursor == initialCursor) {
                return -1;
            }
        }
    }

    public static void FFDispatch(List<MemoryBlock> memoryBlockList, List<Job> waitingJobs, List<Job> runningJobs) {
        int cursor = 0;
        for (int i = 0; i < waitingJobs.size(); ) {
            int backCursor = FFLocate(memoryBlockList, waitingJobs.get(i), cursor);
            if (backCursor == -1) {
                i++;
            } else {
                allocate(memoryBlockList, waitingJobs.get(i), backCursor);
                cursor = ++backCursor;
                runningJobs.add(waitingJobs.get(i));
                waitingJobs.remove(i);
            }
        }
    }

    /*
    this method's former name is FF, but I think it has decoupled from dispatch algorithm,
    so i rename it as simulateContainer
     */
    public static void simulateContainer(List<MemoryBlock> memoryBlockList, List<Job> jobList) {
        int totalJobNum = jobList.size();
        List<Job> runningJobs = new ArrayList<Job>();
        List<Job> overJobs = new ArrayList<Job>();
        List<Job> waitingJobs = jobList;
        while (overJobs.size() < totalJobNum) {
            FFDispatch(memoryBlockList, waitingJobs, runningJobs);

            System.out.println("***************************************");
            System.out.println("running jobs: ");
            showJobList(runningJobs);
            System.out.println("waiting jobs: ");
            showJobList(waitingJobs);
            System.out.println("over jobs: ");
            showJobList(overJobs);

            System.out.println("pre running RAM");
            showMemoryList(memoryBlockList);

            running(memoryBlockList, runningJobs, overJobs);

            System.out.println("post running RAM");
            showMemoryList(memoryBlockList);
        }
    }


    public static void main(String[] args) {
        List<MemoryBlock> memoryBlockList = initMemoryBlocks(10, 1024);
        List<Job> jobList = initJobs(10, 1024);
        simulateContainer(memoryBlockList, jobList);
    }
}
