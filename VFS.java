public class VFS implements Device{


    private VFSMapping[] mappings = new VFSMapping[10];

    /**
     * Opens a device and returns the id of the device
     * @param s The device to open
     * @return The id of the device
     */
    public  int Open(String s) {
        if(s == null || s.isEmpty()){
            return -1;
        }
        String[] split = s.split(" ", 2);
        String deviceInput = split[0].toLowerCase();
        String arguements = split[1];

        // Iterate through the device mappings array to find an empty slot for the new device.
        for(int i = 0; i < mappings.length; i++){
            // If null then a device can be added
            if (mappings[i] == null){
                Device d;
                // Switch statement to determine which device to open
                switch(deviceInput){
                    case "random":
                        d = new RandomDevice();
                        break;
                    case "file":
                        d = new FakeFileSystem();
                        break;
                    default:
                        System.out.println("Device not found in VFS");
                        return -1;
                }
                // Stores the device and the id of the device in the mappings array
                mappings[i] = new VFSMapping(d, d.Open(arguements));
                return i;
            }
        }
        throw new RuntimeException("No space for new device in VFS");
    }

    /**
     * Closes the device with the given id
     * @param id The id of the device
     */
    @Override
    public void Close(int id) {
        if(id >= 0 && id < mappings.length && mappings[id] != null){
            VFSMapping mapping = mappings[id];
            mapping.device.Close(mapping.Did);
            mappings[id] = null;
        }else{
            System.out.println("Error with VFS close");
        }
    }

    /**
     * Reads from the device with the given id
     * @param id The id of the device
     * @param size The size of the data to read
     * @return The data read from the device
     */
    @Override
    public byte[] Read(int id, int size) {
        if( id >= 0 && id < mappings.length && mappings[id] != null) {
            VFSMapping mapping = mappings[id];
            return mapping.device.Read(mapping.Did, size);
        }
        throw new RuntimeException("Error with VFS read");
    }

    /**
     * Seeks to the given location in the device with the given id
     * @param id The id of the device
     * @param to The location to seek to
     */
    @Override
    public void Seek(int id, int to) {
        if( id >= 0 && id < mappings.length && mappings[id] != null){
            VFSMapping mapping = mappings[id];
            mapping.device.Seek(mapping.Did, to);
        }else{
            System.out.println("Error with VFS seek");
        }
    }

    /**
     * Writes to the device with the given id
     * @param id The id of the device
     * @param data The data to write
     * @return The number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        if( id >= 0 && id < mappings.length && mappings[id] != null){
            VFSMapping mapping = mappings[id];
            return mapping.device.Write(mapping.Did, data);
        }
        throw new RuntimeException("Error with VFS write");
    }
}

