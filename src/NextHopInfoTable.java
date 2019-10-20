import java.net.InetAddress;
import java.net.UnknownHostException;

public class NextHopInfoTable {

    InetAddress subnetMask;
    InetAddress neighborAddress;
    Integer hopCount;
    float ttl;

    public NextHopInfoTable(InetAddress subnetMask, InetAddress neighborAddress, Integer hopCount) {
        this.subnetMask = subnetMask;
        this.neighborAddress = neighborAddress;
        this.hopCount = hopCount;
        long time = System.currentTimeMillis();
        this.ttl = (time) / 1000F;
    }


    public void PrintNextHopInfoTable() {
        System.out.print(neighborAddress + "\t\t");
        System.out.println(hopCount);
    }

    public static void main(String[] args) {
        try {
            NextHopInfoTable nhit = new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1);
            nhit.PrintNextHopInfoTable();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
