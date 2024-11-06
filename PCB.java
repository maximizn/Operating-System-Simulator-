import java.util.LinkedList;

public class PCB {

    public int nextPID; // The next process ID to be assigned
    public int PID; // The process ID of the PCB
    UserlandProcess process; // The UserlandProcess of the PCB
    OS.Priority currentP; // The current priority of the process
    int time; // The time used by the process
    int timeout; // The timeout for the process
    int[] integers = new int[10]; // The integers for the process
    public String name;
    public LinkedList<KernelMessage> messageQueue = new LinkedList<>(); // The message queue for the process
    public int []pageTable = new int[Hardware.pageSize]; // The page table for the process

    /**
     * Constructor for the PCB.
     *
     * @param up The UserlandProcess of the PCB.
     * @param currentPriority The priority of the process.
     */
    PCB(UserlandProcess up, OS.Priority currentPriority) {
        this.process = up;
        currentP = currentPriority;
        this.PID = nextPID++;
        this.time = 0;
        this.name = up.getClass().getSimpleName(); // Sets the name using UserlandProcess class name
        messageQueue = new LinkedList<>();
        for(int i = 0; i < integers.length; i++) {
            integers[i] = -1;
        }
        for(int i = 0; i < pageTable.length; i++) {
            pageTable[i] = -1;
        }

    }


    /**
    * Stops the process.
     */
    public void stop(){
        process.stop();
        while(!process.isStopped()){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){ }
        }
    }
    /**
    * Demotes the process to the next priority level.
     */
    public void demote() {
        if (currentP == OS.Priority.REAL) {
            currentP = OS.Priority.INTERACTIVE;
            // Debugging Purposes
            System.out.println("Demoted to INTERACTIVE " + process.toString());
        } else {
            currentP = OS.Priority.BACKGROUND;
            // Debugging Purposes
            System.out.println("Demoted to BACKGROUND " + process.toString());
        }
    }
    /**
    * Calls isDone() from UserlandProcess.
     */
    public boolean isDone(){
        return process.isDone();
    }
    /**
    * Calls start() from UserlandProcess.
     */
    public void start(){
        process.start();
    }
    /**
    * Requests the process to stop and demotes the process if the timeout is greater than 5.
     */
    public void requestStop(){
        process.requestStop();
        timeout++;
        if(timeout > 5){
            timeout = 0;
            if(currentP != OS.Priority.BACKGROUND){
                demote();

            }
        }
    }

}
