import java.io.File;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{

    private RandomAccessFile[] file = new RandomAccessFile[10];

    /**
     * Opens a file and returns the id of the file
     * @param s The name of the file
     * @return The id of the file
     */
    @Override
    public int Open(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < file.length; i++) {
            if (file[i] == null) {
                try {
                    file[i] = new RandomAccessFile(new File(s), "rw");
                    return i;
                } catch (Exception e) {
                    System.out.println("Error with FFS open");
                }
            }
        }
        return -1;
    }

    /**
     * Closes the file with the given id
     * @param id The id of the file
     */
    @Override
    public void Close(int id) {
        if(id < 0 || id >= file.length){
            return;
        }
        try{
            file[id].close();
            file[id] = null;
        }catch (Exception e){
            System.out.println("Error with FFS close");
        }
    }

    /**
     * Reads from the file with the given id
     * @param id The id of the file
     * @param size The size of the data to read
     * @return The data read from the file
     */
    @Override
    public byte[] Read(int id, int size) {

        if( id >= 0 && id < file.length && file[id] != null){
            try{
                byte [] data = new byte[size];
                file[id].read(data);
                return data;
            }catch (Exception e){
                return new byte[0];
            }
        }
        return new byte[0];
    }

    /**
     * Seeks to a position in the file with the given id
     * @param id The id of the file
     * @param to The position to seek to
     */
    @Override
    public void Seek(int id, int to) {
        if( id >= 0 && id < file.length && file[id] != null){
            try{
                file[id].seek(to);
            }catch (Exception e){
                System.out.println("Error with FFS seek");
            }
        }

    }

    /**
     * Writes to the file with the given id
     * @param id The id of the file
     * @param data The data to write to the file
     * @return The number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        if( id >= 0 && id < file.length && file[id] != null){
            try{
                file[id].write(data);
                return data.length;
            }catch (Exception e){
                System.out.println("Error with FFS write");
            }
        }
        return 0;
    }
}
