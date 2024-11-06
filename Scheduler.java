import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.Random;
import java.util.HashMap;

public class Scheduler {

    private PriorityQueue<PCB> sleepingProcesses;// Map of sleeping processes with wakeup times
    private LinkedList<PCB> interactiveQueue;
    private LinkedList<PCB> realQueue;
    private LinkedList<PCB> backgroundQueue;
    static OS.Priority currentPriority;
    private Timer timer; // Timer used to process switching at a 250ms interval
    public PCB currentlyRunning; // Currently running process
    private int processID; // Counter to track next process ID. Used in kernel
    private Clock clock;// For real-time tracking
    private Random ran = new Random();
    private Kernel kernel;
    private HashMap<Integer, PCB> processMap = new HashMap<>();
    private HashMap<Integer, PCB> waitingProcesses = new HashMap<>();
    private boolean[] memoryManager = new boolean[1024];
    private int[][] TLB = new int[2][2];


    public Scheduler(Kernel kernel, HashMap<Integer, PCB> processMap) {
        interactiveQueue = new LinkedList<>(); // Initializes the InteractiveQueue
        realQueue = new LinkedList<>(); // Initializes the realQueue
        backgroundQueue = new LinkedList<>(); // Initializes the BackgroundQueue
        sleepingProcesses = new PriorityQueue<PCB>(Comparator.comparingLong(pcb -> pcb.time)); // Initializes the SleepingProcesses map
        timer = new Timer(); // Initializes timer
        clock = Clock.systemUTC();// Use system clock for time management
        ran = new Random(); // Random object for random process selection
        this.kernel = kernel; // Initializes the kernel
        this.processMap = processMap; // Initializes the process map
        this.waitingProcesses = new HashMap<>(); // Initializes the waiting processes map



        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (currentlyRunning != null) {
                    interrupt();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 250);
    }

    /**
     * Creates a new process and adds it to the scheduler.
     * @param up The UserlandProcess to be added.
     * @return The processID assigned to the newly created process.
     */
    public int CreateProcess(UserlandProcess up) {
      return CreateProcess(up, currentPriority.INTERACTIVE);

    }

    /**
     * Creates a new process and adds it to the scheduler.
     * @param up The UserlandProcess to be added.
     * @param currentPriority The priority of the process.
     * @return The processID assigned to the newly created process.
     */
    public int CreateProcess(UserlandProcess up, OS.Priority currentPriority) {
        PCB pcb = new PCB(up, currentPriority);
        pcb.PID = processID++; // Assign and increment PID
        processMap.put(pcb.PID, pcb); // Add PCB to processMap for quick lookup

        System.out.println("DEBUG: Created process " + up.getClass().getSimpleName() + " with PID " + pcb.PID);
        switch (currentPriority) {
            case REAL:
                realQueue.add(pcb);
                break;
            case INTERACTIVE:
                interactiveQueue.add(pcb);
                break;
            case BACKGROUND:
                backgroundQueue.add(pcb);
                break;
        }

        if (currentlyRunning == null || currentlyRunning.isDone()) {
            SwitchProcess();
        }
        return pcb.PID;
    }


    /**
     * Switches from the currently running process to the next process in the queue.
     */
    public void SwitchProcess() {
        if (currentlyRunning != null) {
            if (!currentlyRunning.isDone()) {
                switch (currentlyRunning.currentP) {
                    case REAL:
                        realQueue.add(currentlyRunning);
                        break;
                    case INTERACTIVE:
                        interactiveQueue.add(currentlyRunning);
                        break;
                    case BACKGROUND:
                        backgroundQueue.add(currentlyRunning);
                        break;
                }
            }
        }
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            wakeUpProcesses();
            randomlyChoose();
    }


    /**
     * Makes the current process sleep for a specified amount of time (ms).
     * @param ms Time to sleep in milliseconds.
     */
    public void SleepProcess(int ms) {

        if (currentlyRunning != null) {
            Instant wakeUpTime = clock.instant().plusMillis(ms);
            // Adds currently running process to the sleepingProcesses map with its wakeup time
            sleepingProcesses.add(currentlyRunning);
            // Debugging purpose
            System.out.println("Process " + currentlyRunning.process.toString() + " is sleeping until " + wakeUpTime);
            randomlyChoose();

        }
    }

    /**
     * Wakes up processes that are scheduled to wake up based on the current time.
     */
    public void wakeUpProcesses() {
        while (!sleepingProcesses.isEmpty()) {
            // Gets the next process to wake up
            PCB p = sleepingProcesses.poll();
            // If the wakeup time is less than or equal to the current time, remove the process from the sleepingProcesses map and add it to the respective queue.
            if (p.time <= clock.millis()) {
          //      System.out.println("Waking up process " + p.name);
                p.time = 0;

                switch (p.currentP) {
                    case REAL:
                        realQueue.addLast(p);
                        break;
                    case INTERACTIVE:
                        interactiveQueue.addLast(p);
                        break;
                    case BACKGROUND:
                        backgroundQueue.addLast(p);
                        break;
                }
            } else {
                sleepingProcesses.add(p); // Re-add the process if it's not time to wake up yet
                break; // Exit the loop since the rest of the processes will also not be ready
            }
        }

    }

    /**
     * Exits the currently running process and removes it from process management structures.
     */
    public void ExitProcess() {
        switch (currentlyRunning.currentP) {
            case REAL:
                realQueue.remove(currentlyRunning);
                break;
            case INTERACTIVE:
                interactiveQueue.remove(currentlyRunning);
                break;
            case BACKGROUND:
                backgroundQueue.remove(currentlyRunning);
                break;
        }
        processMap.remove(currentlyRunning.PID);
        waitingProcesses.remove(currentlyRunning.PID);
        Hardware.clearTlb();


        if (currentlyRunning == null) {
            System.out.println("No process is currently running.");
            return;
        }
       currentlyRunning.requestStop();
        // Remove the process from its priority queue and clean up resources

        try {
            kernel.closeAllDevices();
        } catch (Exception e) {
            System.out.println("Error closing devices: " + e.getMessage().toString());
        }

        randomlyChoose();
    }
    /**
    * Randomly chooses the next process to run following the logic: 60% chance of choosing a real process, 30% chance of choosing an interactive process, and 10% chance of choosing a background process.
    * Also handles the case where if there is only Interactive and Background processes, it will choose an Interactive process 70% of the time and a Background process 30% of the time.
    * if there is only Real and Background processes, it will choose a Real process 90% of the time and a Background process 10% of the time.
    * If there is only Background processes, it will choose a Background process 100% of the time.
    */
    public void randomlyChoose() {
        int prob = ran.nextInt(10);
        if (!realQueue.isEmpty()) {
            if (prob == 6 && !realQueue.isEmpty()) {
                currentlyRunning = realQueue.removeFirst();
            } else if (prob > 0 && prob < 4 && !interactiveQueue.isEmpty()) {
                currentlyRunning = interactiveQueue.removeFirst();
            } else {
                currentlyRunning = realQueue.removeFirst();
            }
            return;

        }
        if (!interactiveQueue.isEmpty()) {
            if (prob == 0 && !backgroundQueue.isEmpty()) {
                currentlyRunning = backgroundQueue.removeFirst();
            } else {
                currentlyRunning = interactiveQueue.removeFirst();
            }
            return;
        }
        if (!backgroundQueue.isEmpty()) {
            currentlyRunning = backgroundQueue.removeFirst();
        }
    }
    /**
     * Handles the timer interrupt and process cooperation logic.
     */
    public void interrupt() {
        if (currentlyRunning != null) {
            currentlyRunning.requestStop();
        } else {
            System.out.println("Error, Process not interrupted");
        }
    }


    // KERNEL ASSIGNMENT-----------------------------------------------------------------------------------------------

    /**
     * Restores a process to its priority queue, used when it becomes runnable.
     * @param process The process to restore.
     */
    public void wakeUpMsg(int process) {
        PCB waitingProcess = waitingProcesses.get(process);
        if (waitingProcess != null) {

            switch (waitingProcess.currentP) {
                case REAL:
                    realQueue.addLast(waitingProcess);
                    break;
                case INTERACTIVE:
                    interactiveQueue.addLast(waitingProcess);
                    break;
                case BACKGROUND:
                    backgroundQueue.addLast(waitingProcess);
                    break;
            }
            waitingProcesses.get(0);
        }
        else{
            System.out.println("Process not found in waiting list.");
        }
    }

    /**
     * Makes the current process wait for a message.
     */
    public void waitForMsg() {
        waitingProcesses.put(currentlyRunning.PID, currentlyRunning);
        //  System.out.println("Process " + currentlyRunning.name + " is now waiting for a message.");
        randomlyChoose();

    }

    /**
     * Checks if a process is in the waiting state for a message.
     * @param pid The process to check.
     * @return true if the process is waiting, false otherwise.
     */
    public boolean isProcessWaitingForMessage(int pid) {
        return waitingProcesses.containsKey(pid);
    }

    /**
     * Gets the currently running process.
     * @return The currently running PCB.
     */
    public PCB getCurrentlyRunning() {
        return currentlyRunning;
    }

    /**
     * Gets the PID of the currently running process.
     * @return The PID of the currently running process, or -1 if no process is running.
     */
    public int getPid() {
        return (currentlyRunning != null) ? currentlyRunning.PID : -1;
    }

    /**
     * Gets the PID of a process by its name.
     * @param name The name of the process.
     * @return The PID of the process, or -1 if not found.
     */
    public int getPidName(String name) {
        for (PCB pcb : processMap.values()) { // Iterate over all PCBs in processMap
       //     System.out.println("DEBUG: Checking PCB with PID " + pcb.PID + " and name " + pcb.name);
            if (pcb.name.equals(name)) {
      //          System.out.println("DEBUG: Found PID " + pcb.PID + " for process name " + name);
                return pcb.PID;
            }
        }
        System.out.println("DEBUG: No process found with name " + name);
        return -1;
    }

    /**
     * Finds and returns a PCB by its PID from the process map.
     * @param pid The PID of the process.
     * @return The PCB with the given PID, or null if not found.
     */
    public PCB findProcessByPid(int pid) {
        return processMap.containsKey(pid) ? processMap.get(pid) : null;
    }

    // Paging ASSIGNMENT-----------------------------------------------------------------------------------------------

    public int allocatePhysPage(){
        for(int i = 0; i < memoryManager.length; i++){
            if(memoryManager[i] == false){
                memoryManager[i] = true;
                return i;
            }
        }
        return -1;
    }

    public int freePhysPage(int pageNum){
        if(pageNum < 0 || pageNum >= memoryManager.length){
            return -1;
        }
        memoryManager[pageNum] = false;
        return 0;
    }

    public int allocateMemory(int size) {
        int pages = size / 1024;
        if (size % 1024 != 0) {
            return -1;
        }
        int consecutivePages = 0;
        int startpage = 0;
        for (int i = 0; i < memoryManager.length; i++) {
            if (memoryManager[i] == false) {
                consecutivePages++;
                if (consecutivePages == pages) {
                    for (int j = i - pages + 1; j <= i; j++) {
                        memoryManager[j] = true;
                    }
                    startpage = i - pages + 1;
                    return startpage;
                }
            } else {
                consecutivePages = 0;
            }

        }
        if (startpage == -1) {
            return -1;
        }
        for(int i = 0; i < pages; i++){
            memoryManager[startpage + i] = true;
        }
        return startpage * 1024;

    }
    public boolean freeMemory(int pageNum, int size){
        int start = pageNum / 1024;
        int pages = size / 1024;

        for(int i = start; i < start + pages; i++){
            if(memoryManager[i] == false || i >= memoryManager.length){
                return false;
            }
            memoryManager[i] = false;
        }
        return true;
    }

    public void updateTlb(int virtualPage, int physicalPage){
        if(TLB[0][0] == -1){
            TLB[0][0] = virtualPage;
            TLB[0][1] = physicalPage;
        }else if(TLB[1][0] == -1){
            TLB[1][0] = virtualPage;
            TLB[1][1] = physicalPage;
        }else{
            TLB[0][0] = TLB[1][0];
            TLB[0][1] = TLB[1][1];
            TLB[1][0] = virtualPage;
            TLB[1][1] = physicalPage;
        }
    }
    public int getPhysPage(int virtualPage){
        for(int i = 0; i < TLB.length; i++){
            if(TLB[i][0] == virtualPage){
                return TLB[i][1];
            }
        }
        return -1;
    }
}
