import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable {
    private Thread thread; // the thread that runs the process
    private Semaphore semaphore; // the semaphore that controls the process
    private  boolean quantumExpired; // the boolean that indicates that the quantum has expired

    public Process()
    {
        semaphore = new Semaphore(0); // the semaphore is initialized to 1
        quantumExpired = false; // the boolean is initialized to false
        thread = new Thread(this); // the thread is initialized with this process
        thread.start(); // the thread is started
    }

    abstract void main() throws InterruptedException;

    /**
     * sets the boolean indicating that this process’ quantum has expired
     */
    public void requestStop()
    {
        quantumExpired = true;
    }

    /**
     * indicates if the semaphore is 0
     * @return true if the semaphore is 0, false otherwise
     */
    public boolean isStopped()
    {
        return semaphore.availablePermits() == 0;
    }

    /**
     *  indicates if the thread is done
     * @return true if the thread is done, false otherwise
     */
    public boolean isDone()
    {
        return !thread.isAlive();
    }

    /**
     *  Releases the semaphore and allows the thread to run
     */
    public void start()
    {
        semaphore.release();
    }

    /**
     *  aqquires the semaphore and stopps the thread from running
     */
    public void stop()
    {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
        }
    }

    /**
     * aqquires the semaphore and calls main()
     */
    public void run()
    {
        try {
            semaphore.acquire();
            main();
        } catch (InterruptedException e) {
        }

    }

    /**
     * if the boolean is true, set the boolean to false and call OS.SwitchProcess()
     */
    public void cooperate()
    {
       if(quantumExpired)
       {
           quantumExpired = false;
           OS.SwitchProcess();
       }
    }

}
