import java.util.Random;
public class RandomDevice implements Device{

    private Random[] ranDevices= new Random[10];

    /**
     * Opens a random device with the given seed
     * @param s The seed for the random device
     * @return The id of the random device
     */
    @Override
    public int Open(String s) {
        for( int i = 0; i < ranDevices.length; i++){
            if (ranDevices[i] == null){
                if(s == null || s.isEmpty()){
                    ranDevices[i] = new Random();
                    return i;
                }else{
                    try{
                        long seed = (Long.parseLong(s));
                        ranDevices[i] = new Random(seed);
                    }catch (Exception e) {
                        ranDevices[i] = new Random(s.hashCode());
                    }
                }
           }
       }
        return -1;
    }

    /**
     * Closes the random device with the given id
     * @param id The id of the random device
     */
    @Override
    public void Close(int id) {
        if(id >= 0 && id < ranDevices.length && ranDevices[id] != null){
            ranDevices[id] = null;
        }else{
            System.out.println("Error with Random Device close");
        }
    }

    /**
     * Reads from the random device with the given id
     * @param id The id of the random device
     * @param size The size of the data to read
     * @return The data read from the random device
     */
    @Override
    public byte[] Read(int id, int size) {
        if( id >= 0 && id < ranDevices.length && ranDevices[id] != null){
            byte [] ranData = new byte[size];
            ranDevices[id].nextBytes(ranData);
            return ranData;
        }
        throw new RuntimeException("Error with Random Device read");
    }

    /**
     * Seeks to a position in the random device with the given id
     * @param id The id of the random device
     * @param to The position to seek to
     */
    @Override
    public void Seek(int id, int to) {
        if( id >= 0 && id < ranDevices.length && ranDevices[id] != null) {
          byte [] ranData = new byte[to];
          ranDevices[id].nextBytes(ranData);
        } else{
            System.out.println("Error with Random Device seek");
            }
    }

    /**
     * Writes to the random device with the given id
     * @param id The id of the random device
     * @param data The data to write to the random device
     * @return The number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
