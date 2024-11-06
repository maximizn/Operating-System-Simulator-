public class Pong extends UserlandProcess {

    private int thisPID;
    private int targetPID;

    public void main() throws InterruptedException {
        thisPID = OS.getPid();
        targetPID = locatePingProcess();

        if (targetPID != -1) {
            System.out.println("I am PONG, ping = " + targetPID);
        } else {
            System.out.println("PONG: Unable to find Ping process.");
        }

        while (true) {
            KernelMessage rcv = OS.waitForMessage();
            if (rcv != null) {
                processReceivedMessage(rcv);
                Thread.sleep(500);
            }
        }
    }

    /**
     * Locates the Ping process by its name.
     *
     * @return The PID of the Ping process.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    private int locatePingProcess() throws InterruptedException {
        int pingPID = OS.getPidbyName("Ping");
        return pingPID;
    }

    /**
     * Processes the received message from the Ping process.
     *
     * @param rcv The received KernelMessage.
     */
    private void processReceivedMessage(KernelMessage rcv) {
        System.out.println("PONG: from: " + rcv.senderPid + " to: " + rcv.TargetPid + " what: " + rcv.data[0]);
        byte nextWhatValue = (byte) (rcv.data[0]);
        sendMessageToPing(nextWhatValue);

    }

    /**
     * Sends a message to the Ping process.
     *
     * @param whatValue The value to send.
     */
    private void sendMessageToPing(byte whatValue) {
        KernelMessage message = new KernelMessage();
        message.senderPid = thisPID;
        message.TargetPid = targetPID;
        message.data = new byte[] { whatValue };
        OS.sendMessage(message);
    }
}
