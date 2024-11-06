public class Hardware {
    public static int pageSize = 1024;
    public static int memorySize = 1024 * pageSize;
    public static byte[] memory = new byte[memorySize];
    public static int[][] Tlb = {{1,1},{1,1}};


    public static void clearTlb() {
        for (int i = 0; i < Tlb.length; i++) {
            Tlb[i][0] = -1;
            Tlb[i][1] = -1;
        }
    }
    public static int getTlbMapping(int virtualPage) {
        for (int i = 0; i < Tlb.length; i++) {
            System.out.println("Checking TLB entry at index " + i + ": Virtual Page "
                    + Tlb[i][0] + " -> Physical Page " + Tlb[i][1]);
            if (Tlb[i][0] == virtualPage) {
                System.out.println("TLB hit: Virtual Page " + virtualPage + " maps to Physical Page " + Tlb[i][1]);
                return Tlb[i][1];  // Correct physical page if found
            }


        }
        return -1;  // Mapping not found
    }
    public static int updateTLB(int virtualPage, int physicalPage) {
        // Check if there is an empty spot (initialized to -1)
        for (int i = 0; i < Tlb.length; i++) {
            if (Tlb[i][0] == -1) {
                Tlb[i][0] = virtualPage;
                Tlb[i][1] = physicalPage;
                System.out.println("Updated TLB: Placed Virtual Page " + virtualPage +
                        " mapped to Physical Page " + physicalPage + " at index " + i);
                return i;
            }
        }

        // If TLB is full, replace a random entry (random replacement strategy)
        int indexToReplace = (int) (Math.random() * Tlb.length);
        Tlb[indexToReplace][0] = virtualPage;
        Tlb[indexToReplace][1] = physicalPage;
        System.out.println("Updated TLB: Replaced entry at index " + indexToReplace +
                " with Virtual Page " + virtualPage +
                " mapped to Physical Page " + physicalPage);

        // Print current TLB state
        System.out.println("Current TLB state after update:");
        for (int i = 0; i < Tlb.length; i++) {
            System.out.println("TLB entry " + i + ": Virtual Page " + Tlb[i][0] + " -> Physical Page " + Tlb[i][1]);
        }
        return indexToReplace;
    }
}
