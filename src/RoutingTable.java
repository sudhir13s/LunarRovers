import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingTable {

    static Map<InetAddress, NextHopInfoTable> routeEntries = new ConcurrentHashMap<>();
    static Integer InterfaceID = 0;

    //    static ArrayList<InetAddress> neighbors = new ArrayList<>();
    static final Object routeEntriesLock = new Object();
//    static boolean isTriggerUpdate = false;

    RoutingTable() {
    }

    public synchronized void PrintRoutingTable() {
        System.out.println("=================================================================================================");
        System.out.println("\t\t\t\t *****Printing routing table info*****");
        System.out.println("=================================================================================================");
        System.out.print("Destination \t");
        System.out.print("Subnet mask \t");
        System.out.print("Next hop \t");
        System.out.print("Interface \t");
        System.out.print("Count \t");
        System.out.print("Flag \t");
        System.out.println("Time");
        NextHopInfoTable nextHopeInfo;
        for (Map.Entry<InetAddress, NextHopInfoTable> route : routeEntries.entrySet()) {
            nextHopeInfo = route.getValue();
            System.out.print(route.getKey() + "\t");
            System.out.print(nextHopeInfo.subnetMask + "\t");
            System.out.print(nextHopeInfo.nextHopAddress + "\t");
            System.out.print(nextHopeInfo.interfaceId + "\t\t");
            System.out.print(nextHopeInfo.hopCount + "\t");
            System.out.print(nextHopeInfo.metricChanged + "\t");
            System.out.println(nextHopeInfo.ttl);
        }

//        for (Map.Entry<InetAddress, InetAddress> route : LunarRover.MAPPING.entrySet()) {
//            System.out.print(route.getKey() + "\t");
//            System.out.println(route.getValue() + "\t");
//        }
        System.out.println("");
    }

//    public static void PrintNeighbor() {
//        for (int i = 0; i < neighbors.size(); i++) {
//            System.out.println("Neighbor: " + neighbors.get(i));
//        }
//    }

    boolean updateRoutingTable(InetAddress destinationAddress, NextHopInfoTable nextHopInfo) {
        boolean isRouteChanged = false;
        // ip check - assumed it is correct.
        // if key exists.
        synchronized (RoutingTable.routeEntriesLock) {
            if (routeEntries.containsKey(destinationAddress)) {
                NextHopInfoTable rtInfo = routeEntries.get(destinationAddress);
                // if not mine entry
                if (rtInfo.hopCount != 0) {

                    // next hope same
                    if (rtInfo.nextHopAddress.equals(nextHopInfo.nextHopAddress)) {
                        if (rtInfo.hopCount > nextHopInfo.hopCount + 1) {
                            nextHopInfo.metricChanged = true;
                            nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                            nextHopInfo.interfaceId = rtInfo.interfaceId;
                            routeEntries.put(destinationAddress, nextHopInfo);
//                            LunarRover.MAPPING.put(destinationAddress, nextHopInfo.nextHopAddress);
//                            System.out.println("RoutingTable: Route table update for " + destinationAddress + nextHopInfo.nextHopAddress + ": " + nextHopInfo.hopCount);
                            isRouteChanged = true;
                        } else {
                            // just update the time.
                            Instant instant = Instant.now();
//                        long time = System.currentTimeMillis();
                            rtInfo.ttl = instant.getEpochSecond();
                            routeEntries.put(destinationAddress, rtInfo);
//                        System.out.println("RoutingTable: Route table update for " + destinationAddress + ": " + rtInfo.ttl);
                        }
                    } else {
                        // different route received for this entry.
                        if (nextHopInfo.hopCount != 16 || rtInfo.hopCount != 16) {
                            if (rtInfo.hopCount > nextHopInfo.hopCount + 1) {
                                nextHopInfo.metricChanged = true;
                                nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                                if (nextHopInfo.interfaceId == 0) {
                                    RoutingTable.InterfaceID = RoutingTable.InterfaceID + 1;
                                    nextHopInfo.interfaceId = RoutingTable.InterfaceID;
                                }
//                                System.out.println("RoutingTable: Different Route table update for " + destinationAddress + ": from:  "
//                                        + rtInfo.nextHopAddress + " to: " + nextHopInfo.nextHopAddress + ": " + nextHopInfo.hopCount);
                                routeEntries.put(destinationAddress, nextHopInfo);
                                LunarRover.MAPPING.put(destinationAddress, nextHopInfo.nextHopAddress);

                                isRouteChanged = true;
                            }
                        }
                    }
                }
            } else {
                // new route, new entry.
                if (nextHopInfo.hopCount < 16) {
                    nextHopInfo.metricChanged = true;
                    // if not rover self entry, increment the count by 1.
//                    if (!LunarRover.MAPPING.containsValue(nextHopInfo.nextHopAddress)) {
//                        nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
//                    }
                    nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                    if (nextHopInfo.interfaceId == 0) {
                        RoutingTable.InterfaceID = RoutingTable.InterfaceID + 1;
                        nextHopInfo.interfaceId = RoutingTable.InterfaceID;
                    }
                    routeEntries.put(destinationAddress, nextHopInfo);
                    LunarRover.MAPPING.put(destinationAddress, nextHopInfo.nextHopAddress);
//                System.out.println("RoutingTable: Route table update - new entry " + destinationAddress + ": " + nextHopInfo.nextHopAddress);
                    isRouteChanged = true;
                }
            }
        }

//        }
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
