public class KernelMessage {
    public int senderPid;
    public int TargetPid;
    public int what;
    public byte[] data;



    /**
     * Constructor for KernelMessage.
     *
     * @param senderPid The PID of the sending process.
     * @param targetPid The PID of the target process.
     * @param what      The message type or identifier.
     * @param data      The data to be sent with the message.
     */
    public KernelMessage(int senderPid, int targetPid, int what, byte[] data) {
        this.senderPid = senderPid;
        this.TargetPid = targetPid;
        this.what = what;
        this.data = data;
    }

    /**
     * Copy constructor for KernelMessage.
     *
     * @param km The KernelMessage to copy.
     */
    public KernelMessage(KernelMessage km) {
        this.senderPid = km.senderPid;
        this.TargetPid = km.TargetPid;
        this.what = km.what;
        this.data = km.data;
    }
    /**
     * Default constructor for KernelMessage.
     */
    public KernelMessage(){}


    /**
     * Returns a string representation of the KernelMessage.
     *
     * @return A string representation of the KernelMessage.
     */
    public String toString() {
        return "KernelMessage{" +
                "senderPid=" + senderPid +
                ", TagretPid=" + TargetPid +
                ", what=" + what +
                ", data=" + data +
                '}';
    }

}
