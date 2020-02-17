import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoutingTable {
    static Map<InetAddress, NextHopInfoTable> ROUTE_ENTRIES = new ConcurrentHashMap<>();
    static Integer InterfaceID = 0;
    static final Object routeEntriesLock = new Object();

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
        for (Map.Entry<InetAddress, NextHopInfoTable> route : ROUTE_ENTRIES.entrySet()) {
            nextHopeInfo = route.getValue();
            System.out.print(route.getKey() + "\t");
            System.out.print(nextHopeInfo.subnetMask + "\t");
            System.out.print(nextHopeInfo.nextHopAddress + "\t");
            System.out.print(nextHopeInfo.interfaceId + "\t\t");
            System.out.print(nextHopeInfo.hopCount + "\t");
            System.out.print(nextHopeInfo.metricChanged + "\t");
            System.out.println(nextHopeInfo.ttl);
        }
    }

    boolean updateRoutingTable(InetAddress destinationAddress, NextHopInfoTable nextHopInfo) {
        boolean isRouteChanged = false;
        // ip check - assumed it is correct.
        // if key exists.
        try {
            if (!destinationAddress.equals(InetAddress.getByName("0.0.0.0"))) {
                synchronized (RoutingTable.routeEntriesLock) {
                    if (ROUTE_ENTRIES.containsKey(destinationAddress)) {
                        NextHopInfoTable rtInfo = ROUTE_ENTRIES.get(destinationAddress);
                        // if not mine entry
                        if (rtInfo.hopCount != 0) {
                            // next hope same
                            if (rtInfo.nextHopAddress.equals(nextHopInfo.nextHopAddress)) {
                                // if same update, just update the time.
                                if (rtInfo.hopCount == nextHopInfo.hopCount + 1) {
                                    Instant instant = Instant.now();
                                    rtInfo.ttl = instant.getEpochSecond();
                                    ROUTE_ENTRIES.put(destinationAddress, rtInfo);
                                }
                                // else update the route.
                                else {
                                    nextHopInfo.metricChanged = true;
                                    if (nextHopInfo.hopCount == 16) {
                                        nextHopInfo.ttl = 0;
                                    } else {
                                        nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                                    }
                                    nextHopInfo.interfaceId = rtInfo.interfaceId;
                                    ROUTE_ENTRIES.put(destinationAddress, nextHopInfo);
                                    if (nextHopInfo.hopCount == 1) {
                                        LunarRover.NEIGHBORS_ENTRIES.put(destinationAddress, nextHopInfo.nextHopAddress);
                                    }
                                    isRouteChanged = true;
                                }
//                                if (rtInfo.hopCount > nextHopInfo.hopCount + 1) {
//                                    nextHopInfo.metricChanged = true;
//                                    nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
//                                    nextHopInfo.interfaceId = rtInfo.interfaceId;
//                                    ROUTE_ENTRIES.put(destinationAddress, nextHopInfo);
//                                    if (nextHopInfo.hopCount == 1) {
//                                        LunarRover.NEIGHBORS_ENTRIES.put(destinationAddress, nextHopInfo.nextHopAddress);
//                                    }
//                                    isRouteChanged = true;
////                            System.out.println("RoutingTable: Route table update for " + destinationAddress + nextHopInfo.nextHopAddress + ": " + nextHopInfo.hopCount);
//
//                                } else {
//                                    // just update the time.
//                                    Instant instant = Instant.now();
//                                    rtInfo.ttl = instant.getEpochSecond();
//                                    ROUTE_ENTRIES.put(destinationAddress, rtInfo);
////                        System.out.println("RoutingTable: Route table update for " + destinationAddress + ": " + rtInfo.ttl);
//                                }
                            } else {
                                // different route received for this entry.
                                if (nextHopInfo.hopCount != 16) {
                                    if (rtInfo.hopCount > nextHopInfo.hopCount + 1) {
                                        nextHopInfo.metricChanged = true;
                                        nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                                        nextHopInfo.interfaceId = rtInfo.interfaceId;
                                        ROUTE_ENTRIES.put(destinationAddress, nextHopInfo);
                                        if (nextHopInfo.hopCount == 1) {
                                            LunarRover.NEIGHBORS_ENTRIES.put(destinationAddress, nextHopInfo.nextHopAddress);
                                        }
                                        isRouteChanged = true;
//                                System.out.println("RoutingTable: Different Route table update for " + destinationAddress + ": from:  "
//                                        + rtInfo.nextHopAddress + " to: " + nextHopInfo.nextHopAddress + ": " + nextHopInfo.hopCount);
                                    }
                                }
                            }
                        }
                    } else {
                        // new route, new entry.
                        if (nextHopInfo.hopCount < 16) {
                            nextHopInfo.metricChanged = true;
                            nextHopInfo.hopCount = nextHopInfo.hopCount + 1;
                            if (nextHopInfo.interfaceId == 0) {
                                nextHopInfo.interfaceId = RoutingTable.InterfaceID;
                                RoutingTable.InterfaceID++;
                            }
                            ROUTE_ENTRIES.put(destinationAddress, nextHopInfo);
//                            System.out.println("RoutingTable: new entry : " + destinationAddress + " : " + nextHopInfo.nextHopAddress + " : " + nextHopInfo.hopCount);
                            if (nextHopInfo.hopCount == 1) {
                                LunarRover.NEIGHBORS_ENTRIES.put(destinationAddress, nextHopInfo.nextHopAddress);
                            }
                            isRouteChanged = true;
                        }
                    }
                }

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
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
