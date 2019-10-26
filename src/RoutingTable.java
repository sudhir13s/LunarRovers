import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable {

    static HashMap<InetAddress, NextHopInfoTable> routeEntries = new HashMap<>();
    static Integer InterfaceID = 0;

//    static ArrayList<InetAddress> neighbors = new ArrayList<>();
    static final Object routeEntriesLock = new Object();
    static boolean isTriggerUpdate = false;

    RoutingTable() {
    }

    public synchronized void PrintRoutingTable() {
        System.out.println("*****Printing routing table info*****");
        System.out.print("Destination \t\t");
        System.out.print("Subnet mask \t\t");
        System.out.print("Next hop \t\t");
        System.out.print("Interface \t");
        System.out.println("Metric count");
        NextHopInfoTable nextHopeInfo;
        for (Map.Entry<InetAddress, NextHopInfoTable> route : routeEntries.entrySet()) {
            nextHopeInfo = route.getValue();
            System.out.print(route.getKey() + "\t\t");
            System.out.print(nextHopeInfo.subnetMask + "\t\t");
            System.out.print(nextHopeInfo.nextHopAddress + "\t\t");
            System.out.print(nextHopeInfo.interfaceId + "\t\t\t");
            System.out.println(nextHopeInfo.hopCount);
        }
    }

//    public static void PrintNeighbor() {
//        for (int i = 0; i < neighbors.size(); i++) {
//            System.out.println("Neighbor: " + neighbors.get(i));
//        }
//    }

    synchronized boolean updateRoutingTable(InetAddress destinationAddress, NextHopInfoTable nextHopInfo) {
        boolean isRouteChanged = false;
        // ip check - assumed it is correct.
        // metric check
        if (nextHopInfo.hopCount < 16) {
            // if key exists.
            if (routeEntries.containsKey(destinationAddress)) {
                NextHopInfoTable rtInfo = routeEntries.get(destinationAddress);
                // next hope same
                if (rtInfo.nextHopAddress.equals(nextHopInfo.nextHopAddress)) {
                    if (rtInfo.hopCount > nextHopInfo.hopCount) {
                        nextHopInfo.metricChanged = true;
                        nextHopInfo.interfaceId = rtInfo.interfaceId;
                        routeEntries.put(destinationAddress, nextHopInfo);
                        System.out.println("RoutingTable: Route table update for " + destinationAddress + ": " + nextHopInfo.hopCount);
                        isRouteChanged = true;
                    } else {
                        // just update the time.
                        Instant instant = Instant.now();
//                        long time = System.currentTimeMillis();
                        rtInfo.ttl = instant.getEpochSecond();
                        routeEntries.put(destinationAddress, rtInfo);
                        System.out.println("RoutingTable: Route table update for " + destinationAddress + ": " + rtInfo.ttl);
                    }
                } else {
                    // different route received for this entry.
                    if (rtInfo.hopCount > nextHopInfo.hopCount) {
                        nextHopInfo.metricChanged = true;
                        if (nextHopInfo.interfaceId == 0) {
                            RoutingTable.InterfaceID = RoutingTable.InterfaceID + 1;
                            nextHopInfo.interfaceId = RoutingTable.InterfaceID;
                        }
                        routeEntries.put(destinationAddress, nextHopInfo);
                        System.out.println("RoutingTable: Route table update for " + destinationAddress + ": " + nextHopInfo.nextHopAddress);
                        isRouteChanged = true;
                    }
                }
            } else {
                // new route, new entry.
                nextHopInfo.metricChanged = true;
                if (nextHopInfo.interfaceId == 0) {
                    RoutingTable.InterfaceID = RoutingTable.InterfaceID + 1;
                    nextHopInfo.interfaceId = RoutingTable.InterfaceID;
                }
                routeEntries.put(destinationAddress, nextHopInfo);
                System.out.println("RoutingTable: Route table update - new entry " + destinationAddress + ": " + nextHopInfo.nextHopAddress);
                isRouteChanged = true;
            }
        }
        return isRouteChanged;
    }

    public static void main(String[] args) {
//        RoutingTable rt = new RoutingTable();
//        try {
//            routeEntries.put(InetAddress.getByName("192.168.56.3"),
//                    new NextHopInfoTable(InetAddress.getByName("255.255.255.0"),
//                            InetAddress.getByName("192.168.56.3"), 1));
////            routeEntries.put(InetAddress.getByName("192.168.1.2"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
////            routeEntries.put(InetAddress.getByName("192.168.1.3"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
////            routeEntries.put(InetAddress.getByName("192.168.1.4"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//
//        rt.PrintRoutingTable();
    }
}
