import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable {

    public static HashMap<InetAddress, NextHopInfoTable> routeEntries;

    public RoutingTable() {
        routeEntries = new HashMap<>();
    }

    public void PrintRoutingTable() {
        NextHopInfoTable nextHopeInfo;
        System.out.println("*****Printing routing table info*****");
        System.out.print("Destination \t\t");
        System.out.print("Subnet mask \t\t");
        System.out.print("Next hop \t\t");
        System.out.println("Count");

        for (Map.Entry<InetAddress, NextHopInfoTable> route : routeEntries.entrySet()) {
            nextHopeInfo = route.getValue();
            System.out.print(route.getKey() + "\t\t");
            System.out.print(nextHopeInfo.subnetMask + "\t\t");
            System.out.print(nextHopeInfo.neighborAddress + "\t\t");
            System.out.println(nextHopeInfo.hopCount);
        }
    }

    public synchronized HashMap<InetAddress, NextHopInfoTable> getRoutingTable() {
        return routeEntries;
    }

    public synchronized void updateRoutingTable(InetAddress destinationAddress, NextHopInfoTable nextHopInfo) {
        routeEntries.put(destinationAddress, nextHopInfo);
        System.out.println("RouteTable: Routing table updated.");
        PrintRoutingTable();
    }

    public static void main(String[] args) {
        RoutingTable rt = new RoutingTable();
        try {
            routeEntries.put(InetAddress.getByName("192.168.56.3"),
                    new NextHopInfoTable(InetAddress.getByName("255.255.255.0"),
                            InetAddress.getByName("192.168.56.3"), 1));
//            routeEntries.put(InetAddress.getByName("192.168.1.2"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//            routeEntries.put(InetAddress.getByName("192.168.1.3"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//            routeEntries.put(InetAddress.getByName("192.168.1.4"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        rt.PrintRoutingTable();
    }
}
