public class Kernel extends Process implements Device {

    Scheduler scheduler = new Scheduler(this, OS.getProccessMap()); // Scheduler object to manage processes
    VFS VFS = new VFS(); // Virtual File System object to manage devices

    @Override
    public void main() {
        run();
    }

    public void run() {
        while (true) {
            stop();
            switch (OS.currentCall) {
                // Switches the process
                case CREATE_PROCESS:
                    OS.returnValue = scheduler.CreateProcess((UserlandProcess) OS.getParameters().get(0));
                    break;
                case SWITCH_PROCESS:
                    // Switches the process
                    scheduler.SwitchProcess();
                    break;
                // Sleeps the process
                case SLEEP_PROCESS:
                    scheduler.SleepProcess((int) OS.getParameters().get(0));
                    break;
                // Exits the process
                case EXIT_PROCESS:
                    scheduler.ExitProcess();
                    break;
                    // Waits for a message
                case WAIT_FOR_MESSAGE:
                    OS.returnValue = waitForMessage();
                    break;
                    // Sends a message
                case SEND_MESSAGE:
                    sendMessage((KernelMessage) OS.getParameters().get(0));
                    break;
                    // Gets the PID of the currently running process
                case GET_PID:
                    OS.returnValue = scheduler.getPid();
                    break;
                    // Gets the PID of a process by name
                case GET_PID_BY_NAME:
                    OS.returnValue = scheduler.getPidName((String) OS.getParameters().get(0));
                    break;
                    // Fetches a message
                case FETCH_MESSAGE:
                    OS.returnValue = FetchMessage();
                    break;
                    // Opens a device
                case OPEN:
                    OS.returnValue = Open((String) OS.getParameters().get(0));
                    break;
                    // Closes a device
                case CLOSE:
                    Close((int) OS.getParameters().get(0));
                    break;
                    // Reads from a device
                case READ:
                    OS.returnValue = Read((int) OS.getParameters().get(0), (int) OS.getParameters().get(1));
                    break;
                    // Seeks to a position in a device
                case SEEK:
                    Seek((int) OS.getParameters().get(0), (int) OS.getParameters().get(1));
                    break;
                    // Writes to a device
                case WRITE:
                    OS.returnValue = Write((int) OS.getParameters().get(0), (byte[]) OS.getParameters().get(1));
                    break;
                case AllOCATE_MEMORY:
                    OS.returnValue = scheduler.allocateMemory((int) OS.getParameters().get(0));
                    break;
                case RELEASE_MEMORY:
                    OS.returnValue = scheduler.freeMemory((int) OS.getParameters().get(0), (int) OS.getParameters().get(1));
                    break;
                case GET_MAPPING:
                    getMapping((int) OS.getParameters().get(0));
                    break;


                default:
                    System.out.println("Unsupported system call in Kernel");
                    break;
            }
            scheduler.currentlyRunning.start();
        }
    }

    /**
     * Opens a device for the currently running process by interacting with the VFS.
     *
     * @param s The name of the file to open
     * @return The index of the opened device in the process's device list, or -1 if an error.
     */
    @Override
    public int Open(String s) {
        PCB currentProccess = scheduler.getCurrentlyRunning();
        if (currentProccess == null) {
            return -1;
        }
        for (int i = 0; i < currentProccess.integers.length; i++) {
            if (currentProccess.integers[i] == -1) {
                int vfsId = VFS.Open(s);
                if (vfsId == -1) {
                    System.out.println("VFS Open came back with -1");
                    return -1;
                } else {
                    currentProccess.integers[i] = vfsId;
                    System.out.println("Opened device at PCB index " + i + " with VFS ID: " + vfsId);
                    return i;
                }
            } else {
                System.out.println("Current process has no space for new device");
                return -1;
            }

        }
        System.out.println("No space for new device in Kernel");
        return -1;
    }

    /**
     * Closes a device that was previously opened by the current process.
     *
     * @param id The index of the device to close in the process's device list.
     */
    @Override
    public void Close(int id) {
        PCB currentProccess = scheduler.getCurrentlyRunning();
        if (currentProccess == null) {
            return;
        }
        if (id >= 0 && id < currentProccess.integers.length && currentProccess.integers[id] != -1) {
            VFS.Close(currentProccess.integers[id]);
            currentProccess.integers[id] = -1;
            System.out.println("Closed device at PCB index " + id);
        } else {
            System.out.println("Error with Kernel close");
        }

    }

    /**
     * Reads from a device that was previously opened by the current process.
     *
     * @param id The index of the device to read from in the process's device list.
     */
    @Override
    public byte[] Read(int id, int size) {
        PCB currentProccess = scheduler.getCurrentlyRunning();
        if (currentProccess == null) {
            return null;
        }
        if (id >= 0 && id < currentProccess.integers.length && currentProccess.integers[id] != -1) {
            return VFS.Read(currentProccess.integers[id], size);
        } else {
            System.out.println("Error with Kernel read");
        }
        return null;
    }

    /**
     * Seeks to a position in a device that was previously opened by the current process.
     *
     * @param id The index of the device to seek in the process's device list.
     * @param to The position to seek to.
     */
    @Override
    public void Seek(int id, int to) {
        PCB currentProccess = scheduler.getCurrentlyRunning();
        if (currentProccess == null) {
            return;
        }
        if (id >= 0 && id < currentProccess.integers.length && currentProccess.integers[id] != -1) {
            VFS.Seek(currentProccess.integers[id], to);
        } else {
            System.out.println("Error with Kernel seek");
        }
    }

    /**
     * Writes to a device that was previously opened by the current process.
     *
     * @param id   The index of the device to write to in the process's device list.
     * @param data The data to write to the device.
     * @return The number of bytes written, or -1 if an error.
     */
    @Override
    public int Write(int id, byte[] data) {
        PCB currentProccess = scheduler.getCurrentlyRunning();
        if (currentProccess == null) {
            return -1;
        }
        if (id >= 0 && id < currentProccess.integers.length && currentProccess.integers[id] != -1) {
            return VFS.Write(currentProccess.integers[id], data);
        } else {
            System.out.println("Error with Kernel write");
        }
        return -1;
    }

    /**
     * Closes all devices for a given process.
     */
    public void closeAllDevices() {
        PCB currentProcess = scheduler.getCurrentlyRunning();
        for (int i = 0; i < currentProcess.integers.length; i++) {
            if (currentProcess.integers[i] != -1) {
                VFS.Close(currentProcess.integers[i]);
                currentProcess.integers[i] = -1;
            }
        }
        System.out.println("Closed all devices for process " + currentProcess.integers.toString());
    }

    // KERNEL ASSIGNMENT-----------------------------------------------------------------------------------------------

    /**
     * Sends a message to the target process's message queue.
     *
     * @param km The KernelMessage to be sent.
     */
    private void sendMessage(KernelMessage km) {
       KernelMessage msg = new KernelMessage(km);
       msg.senderPid = scheduler.getPid();
       PCB targetProcess = scheduler.findProcessByPid(msg.TargetPid);
 //       System.out.println("Sending message from process " + km.senderPid + " to process " + km.TargetPid);

        if (targetProcess != null) {
            targetProcess.messageQueue.add(msg);
  //          System.out.println("Message sent to process " + targetProcess.name);
            if(scheduler.isProcessWaitingForMessage(targetProcess.PID)) {
 //               System.out.println("Waking up target process " + targetProcess.name);
                scheduler.wakeUpMsg(targetProcess.PID);
            }
        } else {
            System.out.println("Error: Target process with PID " + km.TargetPid + " not found.");
        }
    }

    /**
     * Waits for a message to arrive in the currently running process's message queue.
     *
     * @return The received KernelMessage, or null if none is available.
     */
    private KernelMessage waitForMessage() {
  //      System.out.println("getting to scheduler wait for msg");
        if (!scheduler.currentlyRunning.messageQueue.isEmpty()) {
 //           System.out.println("Message received by process " + scheduler.currentlyRunning.name);
            return scheduler.currentlyRunning.messageQueue.remove();
        }
            scheduler.waitForMsg();
            return null;

    }

    /**
     * Fetches a message from the currently running process's message queue.
     *
     * @return The fetched KernelMessage, or null if none is available.
     */
    private KernelMessage FetchMessage() {
        if (!scheduler.currentlyRunning.messageQueue.isEmpty()) {
            return scheduler.currentlyRunning.messageQueue.remove();
        }
        return null;

    }
    //Pagging Assignment-----------------------------------------------------------------------------------------------
    public void getMapping(int vPage){
        PCB currentProccess = scheduler.getCurrentlyRunning();
        int physPage = currentProccess.pageTable[vPage];
        if(physPage == -1){
            physPage = scheduler.allocatePhysPage();
            currentProccess.pageTable[vPage] = physPage;
        }
        scheduler.updateTlb(vPage, physPage);
        int check = Hardware.updateTLB(vPage, physPage);
        if (check == -1){
            System.out.println("Warning: TLB update for Virtual Page " + vPage +
                    " to Physical Page " + physPage + " failed verification.");
        } else {
            System.out.println("Verified TLB mapping: Virtual Page " + vPage +
                    " successfully maps to Physical Page " + check);
        }
    }
}
