import java.util.ArrayList;
import java.util.HashMap;

// Enumerations for the CallType and Priority

public class OS {
    public enum CallType {
        CREATE_PROCESS, SWITCH_PROCESS, EXIT_PROCESS, SLEEP_PROCESS, WAIT_FOR_MESSAGE, SEND_MESSAGE, GET_PID,
        GET_PID_BY_NAME, FETCH_MESSAGE, OPEN, CLOSE, SEEK, READ, WRITE, AllOCATE_MEMORY, GET_MAPPING, RELEASE_MEMORY
    }

    public enum Priority {
        REAL, INTERACTIVE, BACKGROUND
    }
    private static HashMap<Integer, PCB> processMap = new HashMap<>(); // Maps process IDs to UserlandProcesses
    public static HashMap<Integer, PCB> getProccessMap(){
        return processMap;
    }

    static CallType currentCall; // The current call type
    static Priority currentPriority; /// The current priority
    private static ArrayList<Object> parameters = new ArrayList<>(); // The parameters
    public static Object returnValue; // The return value
    private static Kernel kernel; // The kernel

    /**
     * Creates a new process.
     */
    public static int CreateProcess(UserlandProcess up) throws InterruptedException {
           // Resets the parameters
            parameters.clear();
            // Adds the UserlandProcess
            parameters.add(up);
            // Sets the current call type
            currentCall = CallType.CREATE_PROCESS;
            // Switches to Kernel
            kernel.start();
            // Stops the currently running process
           if(kernel.scheduler.currentlyRunning != null){
               kernel.scheduler.currentlyRunning.stop();
           }else {
               // Loops with sleep(10) until there is a currentProcess available
               while (kernel.scheduler.currentlyRunning == null) {
                   try {
                       Thread.sleep(10);
                   } catch (InterruptedException e) {
                   }
               }
           }
            //  Casts and returns the process id
            return (int) (returnValue);
    }

    /**
     * Switches the currently running process.
     */
    public static int CreateProcess(UserlandProcess up, Priority currentPriority){
        // Resets the parameters
        parameters.clear();
        // Adds the UserlandProcess
        parameters.add(up);
        // Adds the current priority
        parameters.add(currentPriority);
        // Sets the current call type
        currentCall = CallType.CREATE_PROCESS;
        // Switches to Kernel
        kernel.start();

        if(kernel.scheduler.currentlyRunning != null){
            kernel.scheduler.currentlyRunning.stop();
        }
        else {
            // Loops with sleep(10) until there is a currentProcess available
            while (kernel.scheduler.currentlyRunning == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        }
        //  Casts and returns the process id
        return (int) (returnValue);
    }

    /**
     * Switches the currently running process.
     */
    public static void SwitchProcess() {
        // Clears parameters
        parameters.clear();
        // Adds the currently running process
        parameters.add(kernel.scheduler.currentlyRunning);
        // Sets the current call type
        currentCall = CallType.SWITCH_PROCESS;
        PCB currentProccess = kernel.scheduler.currentlyRunning;
        // Switches to the kernel
        kernel.start();
        // Stops the currently running process
        if(kernel.scheduler.currentlyRunning != null){
            currentProccess.stop();
        }
    }
    /**
    * Sleep for a given amount of time
     */
    public static void Sleep(int ms){
        // Clears parameters
        parameters.clear();
        // Adds the time to sleep
        parameters.add(ms);
        // Sets the current call type
        currentCall = CallType.SLEEP_PROCESS;
        PCB currentProccess = kernel.scheduler.currentlyRunning;
        // Switches to the kernel
        kernel.start();
        // Stops the currently running process
        if(kernel.scheduler.currentlyRunning != null){
            currentProccess.stop();
        }
    }

    /**
    * Starts the OS
     */
    public static void StartUp(UserlandProcess init) throws InterruptedException {
        kernel = new Kernel();
        CreateProcess(init);
        CreateProcess(new IdleProcess());
    }


    /**
     * Exits the currently running process.
     */
    public static void ExitProcess() {
        // Clears parameters
       parameters.clear();
       // Sets the current call type
       currentCall = CallType.EXIT_PROCESS;
       PCB currentProccess = kernel.scheduler.currentlyRunning;
       // Switches to the kernel
       kernel.start();
       // Stops the currently running process
      currentProccess.stop();
    }
    /**
     * Gets the PID of the currently running process.
     *
     * @return The PID of the currently running process.
     */
    public static int getPid(){
        currentCall = CallType.GET_PID;
        returnValue = null;
        PCB currentProccess = kernel.scheduler.currentlyRunning;
        kernel.start();
        currentProccess.stop();
        while (returnValue == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return (int) returnValue;
    }
    /**
     * Gets the PID of a process by its name.
     *
     * @param name The name of the process to find.
     * @return The PID of the process, or -1 if not found.
     */
    public static int getPidbyName(String name) {
        parameters.clear();
        currentCall = CallType.GET_PID_BY_NAME;
        returnValue = null;
        parameters.add(name);
        PCB currentProccess = kernel.scheduler.currentlyRunning;
        kernel.start();
        currentProccess.stop();

        while (returnValue == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return (int) returnValue;
    }
    /**
     * Sends a message to a target process.
     *
     * @param km The KernelMessage to send.
     */
   public static void sendMessage(KernelMessage km){
       currentCall = CallType.SEND_MESSAGE;
       parameters = new ArrayList<>();
       parameters.add(km);
       PCB currentProccess = kernel.scheduler.currentlyRunning;
       kernel.start();
       currentProccess.stop();
    }
    /**
     * Waits for a message to be received.
     *
     * @return The received KernelMessage.
     */
   public static KernelMessage waitForMessage() {
        currentCall = CallType.WAIT_FOR_MESSAGE;
        PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
        kernel.start();
        currentProcess.stop();
        if (!(returnValue instanceof KernelMessage)) {
            currentCall = CallType.FETCH_MESSAGE;
            kernel.start();
            currentProcess.stop();
            while (!(returnValue instanceof KernelMessage)) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
        }
        return (KernelMessage) returnValue;
    }

    /**
     * Closes a device for the currently running process by interacting with the VFS.
     *
     * @param id The index of the device to close
     */
    public static void Close(int id){
        currentCall = CallType.CLOSE;
        parameters.clear();
        parameters.add(id);
        PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
        kernel.start();
        currentProcess.stop();
    }
    /**
     * Opens a device for the currently running process by interacting with the VFS.
     *
     * @param s The name of the file to open
     * @return The index of the opened device in the process's device list, or -1 if an error.
     */
    public static int Open(String s) {
        currentCall = CallType.OPEN;
        parameters.clear();
        parameters.add(s);
        PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
        kernel.start();
        currentProcess.stop();
        while(returnValue == null){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return (int) returnValue;
    }
    /**
     * Gets the current call type
     * @return The current call type
     */
    public static CallType getCurrentCall() {
        return currentCall;
    }
    /**
     * Gets the parameters
     * @return The parameters
     */
    public static ArrayList<Object> getParameters() {
        return parameters;
    }
    /**
     * Gets the return value
     * @return The return value
     */
    public static void Seek(int id, int position){
        currentCall = CallType.SEEK;
        parameters.clear();
        parameters.add(id);
        parameters.add(position);
        PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
        kernel.start();
        currentProcess.stop();
    }
    /**
     * Writes to a device
     * @param id The index of the device to write to
     * @param data The data to write
     * @return The number of bytes written, or -1 if an error occurred
     */
    public static int Write(int id, byte[] data){
        currentCall = CallType.WRITE;
        parameters.clear();
        parameters.add(id);
        parameters.add(data);
        PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
        kernel.start();
        currentProcess.stop();
        while(returnValue == null){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }
        return (int) returnValue;
    }
        /**
         * Reads from a device
         * @param id The index of the device to read from
         * @param length The number of bytes to read
         * @return The data read from the device, or null if an error occurred
         */
        public static byte[] Read(int id, int length){
            currentCall = CallType.READ;
            parameters.clear();
            parameters.add(id);
            parameters.add(length);
            PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
            kernel.start();
            currentProcess.stop();
            while(returnValue == null){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            return (byte[]) returnValue;
        }
        //--------------------------------PAGGING ASSIGNMENT-----------------------------------------------------------

        public static int allocateMemory(int size){
            currentCall = CallType.AllOCATE_MEMORY;
            parameters.clear();
            parameters.add(size);
            PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
            kernel.start();
            currentProcess.stop();
            while(returnValue == null){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            return (int) returnValue;
        }
        public static void getMapping(int virtualPage){
            currentCall = CallType.GET_MAPPING;
            parameters.clear();
            parameters.add(virtualPage);
            PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
            kernel.start();
            currentProcess.stop();
        }
        public static boolean releaseMemory(int physicalPage, int size){
            currentCall = CallType.RELEASE_MEMORY;
            parameters.clear();
            parameters.add(physicalPage);
            parameters.add(size);
            PCB currentProcess = kernel.scheduler.getCurrentlyRunning();
            kernel.start();
            currentProcess.stop();
            while(returnValue == null){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                }
            }
            return (boolean) returnValue;
        }
}
