public abstract class UserlandProcess extends Process{
    public byte Read(int id){
        int virtualPage = id / Hardware.pageSize;
        int offset = id % Hardware.pageSize;
        int physicalPage = Hardware.getTlbMapping(virtualPage);

        if(physicalPage == -1){
             OS.getMapping(virtualPage);
             physicalPage = Hardware.getTlbMapping(virtualPage);
             System.out.println("Out of getTlbMapping");
             if(physicalPage == -1){
                    return -1;
             }

        }
        int physicalAddress = physicalPage * Hardware.pageSize + offset;
        return Hardware.memory[physicalAddress];
    }
    public void Write(int id, byte data){
        int virtualPage = id / Hardware.pageSize;
        int offset = id % Hardware.pageSize;
        int physicalPage = Hardware.getTlbMapping(virtualPage);
        if(physicalPage == -1){
             OS.getMapping(virtualPage);
             physicalPage = Hardware.getTlbMapping(virtualPage);
             if(physicalPage == -1){
                 return;
             }

        }
        int physicalAddress = physicalPage * Hardware.pageSize + offset;
        Hardware.memory[physicalAddress] = data;


    }


}
