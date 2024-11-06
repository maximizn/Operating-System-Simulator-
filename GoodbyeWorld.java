public class GoodbyeWorld extends UserlandProcess{

    @Override
    public void main() {
        while(true)
        {
            System.out.println("Goodbye World");
            // Sleeping for 10 milliseconds, Checking if OS.Sleep works as expected.
           OS.Sleep(10);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            int fakeFile = OS.Open("file fakeFile.dat");
//            if(fakeFile == -1){
//                System.out.println("Error opening file");
            cooperate();
            }


        }

    // For debugging purposes
    public String toString(){
        return "GoodbyeWorld";
    }
}
