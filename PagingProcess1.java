public class PagingProcess1 extends UserlandProcess{

    public void main(){
        int startAddr = OS.allocateMemory(3072);
        System.out.println("PAGING PROCESS 2: Allocated 3 pages of memory. start addr: " + startAddr);
        try{
            Thread.sleep(250);
        }
        catch(InterruptedException e){
            throw new RuntimeException(e);
        }
        System.out.println("PAGING PROCESS 2: writing val 48 to addr: " + (startAddr + 1999));
        Write(startAddr + 1999, (byte) 48);
        try{
            Thread.sleep(250);
        }
        catch(InterruptedException e){
            throw new RuntimeException(e);
        }
        cooperate();
        System.out.println("PAGING PROCESS 2: reading val from addr: " + (startAddr + 1999));
        byte read = Read(startAddr + 1999);
        System.out.println("PAGING PROCESS 2: val: " + read);
        try{
            Thread.sleep(250);
        }
        catch(InterruptedException e){
            throw new RuntimeException(e);
        }
        cooperate();
        int newStartAddr = OS.allocateMemory(1024);
        System.out.println("PAGING PROCESS 2: Allocated a new page. new start addr: " + newStartAddr);
        Write(newStartAddr + 500, (byte) 30);
        System.out.println("PAGING PROCESS 2: Wrote 30 to addr: " + (newStartAddr + 500));
        byte newRead = Read(newStartAddr + 500);
        System.out.println("PAGING PROCESS 2: val: " + newRead);
        OS.releaseMemory(newStartAddr, 1024);
        OS.ExitProcess();
    }


}