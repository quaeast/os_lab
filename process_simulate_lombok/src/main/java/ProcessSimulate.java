import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
class PCB {
    private String name;
    // 0 for ready, 1 for wait
    private int status;
    private int priority;
    // from 1 ro 10
    private int time;
}


@FunctionalInterface
interface ProcessSchedulePickMechanism {
    public int pick(List<PCB> readyList);
}


public class ProcessSimulate {
    // [0, 1]
    public static int getRandom() {
        return getRandom(0, 1);
    }

    // [1, 10]
    public static int getRandomTen() {
        return getRandom(1, 10);
    }

    //[begin, end]
    public static int getRandom(int begin, int end) {
        double segment = 1.0 / (1 + end - begin);
        return begin + (int) (Math.random() / segment);
    }

    public static List<PCB> init(int length) {
        List<PCB> pcbList = new ArrayList<PCB>();
        List<Integer> pcbPriority = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            pcbPriority.add(i + 1);
        }
        Collections.shuffle(pcbPriority);
        for (int i = 0; i < 10; i++) {
            String name = "process" + i;
            pcbList.add(new PCB(name, getRandom(), pcbPriority.get(i), getRandomTen()));
        }
        return pcbList;
    }

    public static int runProcess(PCB pcb) {
        int runtime = getRandom(1, pcb.getTime());
        pcb.setTime(pcb.getTime() - runtime);
        if (pcb.getTime() == 0) {
            pcb.setStatus(9);
        }
        System.out.println(pcb.getName() + " is running for: " + runtime + "ms");
        return runtime;
    }

    public static void regenerateStatus(List<PCB> readyList, List<PCB> waitList) {
        for (PCB i : readyList) {
            i.setStatus(getRandom());
        }
        for (PCB i : waitList) {
            i.setStatus(getRandom());
        }
        for (int i = 0; i < readyList.size(); ) {
            if (readyList.get(i).getStatus() == 0) {
                i++;
            } else {
                waitList.add(readyList.get(i));
                readyList.remove(i);
            }
        }
        for (int i = 0; i < waitList.size(); ) {
            if (waitList.get(i).getStatus() == 1) {
                i++;
            } else {
                readyList.add(waitList.get(i));
                waitList.remove(i);
            }
        }
    }

    public static int FIFOPick(List<PCB> readyList) {
        if (readyList.isEmpty())
            return -1;
        return 0;
    }

    public static int PriorityPick(List<PCB> readyList) {
        int maxPriority = -1;
        int resultPosition = -1;
        for (int i = 0; i < readyList.size(); i++) {
            if (readyList.get(i).getPriority() > maxPriority) {
                maxPriority = readyList.get(i).getPriority();
                resultPosition = i;
            }
        }
        return resultPosition;
    }

    public static int LongestTimePick(List<PCB> readyList) {
        int longestTime = -1;
        int resultPosition = -1;
        for (int i=0; i<readyList.size();i++){
            if (readyList.get(i).getTime()>longestTime){
                longestTime = readyList.get(i).getPriority();
                resultPosition = i;
            }
        }
        return longestTime;
    }

    public static void simulateContainer(List<PCB> pcbList, ProcessSchedulePickMechanism processSchedulePickMechanism) {
        List<PCB> readyList = new ArrayList<PCB>();
        List<PCB> waitList = new ArrayList<PCB>();
        List<PCB> overList = new ArrayList<PCB>();
        for (PCB i : pcbList) {
            if (i.getStatus() == 0) {
                readyList.add(i);
            } else {
                waitList.add(i);
            }
        }
        while (overList.size() < pcbList.size()) {
            System.out.println("****************************************");
            System.out.println("ready list: ");
            showPcbList(readyList);
            System.out.println("wait list: ");
            showPcbList(waitList);
            System.out.println("over list: ");
            showPcbList(overList);
            if (readyList.size() != 0) {
                int position = processSchedulePickMechanism.pick(readyList);
                PCB runningPrecess = readyList.get(position);
                runProcess(runningPrecess);
                if (runningPrecess.getStatus() == 9) {
                    System.out.println(runningPrecess.getName() + " is over");
                    overList.add(runningPrecess);
                    readyList.remove(position);
                }
            }
            regenerateStatus(readyList, waitList);
        }
    }

    public static void showPcbList(List<PCB> pcbList) {
        for (PCB i : pcbList) {
            System.out.print(i.getName() + ": ");
            System.out.print(i.getTime() + "ms, ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        List<PCB> pcbList = init(10);
        simulateContainer(pcbList, ProcessSimulate::FIFOPick);
    }
}
