public class Init extends UserlandProcess {
    @Override
    public void main(){

        try {
            OS.CreateProcess(new HelloWorld());
            OS.CreateProcess(new GoodbyeWorld());
            OS.CreateProcess(new Ping(), OS.Priority.INTERACTIVE);
            OS.CreateProcess(new Pong(),OS.Priority.INTERACTIVE);
            OS.CreateProcess(new PagingProcess(), OS.Priority.INTERACTIVE);
            OS.CreateProcess(new PagingProcess1(), OS.Priority.INTERACTIVE);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
