public class Ping extends UserlandProcess {

    private int thisPID;
    private int targetPID;

    public void main() throws InterruptedException {
        thisPID = OS.getPid();
        targetPID = locatePongProcess();
        System.out.println("I am PING, pong = " + targetPID);
        sendMessageToPong((byte) 0); // Start the message chain with an initial value of 0

        while (true) {
            KernelMessage rcv = OS.waitForMessage();
            if (rcv != null) {
                processReceivedMessage(rcv);
                Thread.sleep(500);
            }
        }
    }

    /**
     * Locates the Pong process by its name.
     *
     * @return The PID of the Pong process.
     * @throws InterruptedException If the thread is interrupted while waiting.
     */
    private int locatePongProcess() throws InterruptedException {
        int pongPID = -1;
        while (pongPID == -1) {
            Thread.sleep(50); // Short wait
            pongPID = OS.getPidbyName("Pong");
        }
        return pongPID;
    }

    /**
     * Sends a message to the Pong process.
     *
     * @param whatValue The value to send.
     */
    private void sendMessageToPong(byte whatValue) {
        KernelMessage message = new KernelMessage();
        message.senderPid = thisPID;
        message.TargetPid = targetPID;
        message.data = new byte[] { whatValue };
        OS.sendMessage(message);
    }

    /**
     * Processes the received message from the Pong process.
     *
     * @param rcv The received KernelMessage.
     */
    private void processReceivedMessage(KernelMessage rcv) {
        System.out.println("PING: from: " + rcv.senderPid + " to: " + rcv.TargetPid + " what: " + rcv.data[0]);
        byte nextWhatValue = (byte) (++rcv.data[0]);
        sendMessageToPong(nextWhatValue);
    }
}
