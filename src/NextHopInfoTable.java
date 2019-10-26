import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

public class NextHopInfoTable {

    InetAddress subnetMask;
    InetAddress nextHopAddress;
    Integer interfaceId;
    Integer hopCount;
    boolean metricChanged;
    long ttl;

    public NextHopInfoTable(InetAddress subnetMask, InetAddress nextHopAddress, Integer interfaceId, Integer hopCount) {
        this.subnetMask = subnetMask;
        this.nextHopAddress = nextHopAddress;
        this.interfaceId = interfaceId;
        this.hopCount = hopCount;
        this.metricChanged = false;
        Instant instant = Instant.now();
//        long time = System.currentTimeMillis();
//        this.ttl = (time) / 1000F;
        this.ttl = instant.getEpochSecond();
    }

    public void PrintNextHopInfoTable() {
        System.out.print(this.subnetMask + "\t\t");
        System.out.print(this.nextHopAddress + "\t\t");
        System.out.print(this.interfaceId + "\t\t");
        System.out.print(this.hopCount + "\t\t");
        System.out.print(this.metricChanged + "\t\t");
        System.out.println(this.ttl);
    }

    public static void main(String[] args) {
        try {
            NextHopInfoTable nhit = new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1, 1);
            nhit.PrintNextHopInfoTable();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
