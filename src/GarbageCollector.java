import java.net.InetAddress;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

public class GarbageCollector implements Runnable {

    private void RemoveUnreachableRoutesFromRoutingTable() {
        NextHopInfoTable nextHopeInfo;
        boolean isTriggerUpdate = false;
        RoutingTable rt = new RoutingTable();

//            System.out.println("In synchronized");
        Iterator<Map.Entry<InetAddress, NextHopInfoTable>> iterator = RoutingTable.routeEntries.entrySet().iterator();
//        synchronized (RoutingTable.routeEntriesLock) {
            while (iterator.hasNext()) {
                Map.Entry<InetAddress, NextHopInfoTable> route = iterator.next();
                nextHopeInfo = route.getValue();
                Instant instant = Instant.now();
                // Rover self entry. don't remove, rather update time.
                if (nextHopeInfo.hopCount == 0) {
                    nextHopeInfo.ttl = instant.getEpochSecond();
                    RoutingTable.routeEntries.put(route.getKey(), nextHopeInfo);
//                    System.out.println("update self timer.");
                }
                // remaining entries.
                else if (nextHopeInfo.hopCount > 0) {
//                    System.out.println("GarbageCollector: Inside hopCount > 0");
                    // means the route is not reachable. update expire entry or remove it.
                    if (nextHopeInfo.ttl == 0 && nextHopeInfo.hopCount == 16 && !nextHopeInfo.metricChanged) {
//                        System.out.println("GarbageCollector: Removing entry: " + route.getKey());
                        iterator.remove();
                        LunarRover.MAPPING.remove(route.getKey());
                    } else if ((instant.getEpochSecond() - nextHopeInfo.ttl > 5) && (nextHopeInfo.hopCount != 16)) {
                        nextHopeInfo.ttl = 0;
                        nextHopeInfo.hopCount = 16;
                        nextHopeInfo.metricChanged = true;
                        RoutingTable.routeEntries.put(route.getKey(), nextHopeInfo);
                        isTriggerUpdate = true;
//                        System.out.println("GarbageCollector: updated ttl time to zero for entry: " + route.getKey());
                    }
//                    System.out.println("GarbageCollector: " + instant.getEpochSecond() + " - " + nextHopeInfo.ttl);
                }
            }
//        }

        // if RoutingTable.isTriggerUpdate flag is set, send trigger update.
        if (isTriggerUpdate) {
//            System.out.println("GarbageCollector: RIP Trigger update called.");
            rt.PrintRoutingTable();
            RIPTriggerUpdate.isTriggerUpdate = true;
            isTriggerUpdate = false;
//            Thread ripTriggerUpdate = new Thread(new RIPTriggerUpdate(LunarRover.MULTICAST_ADDRESS, LunarRover.MULTICAST_PORT), "RIP Trigger Update");
//            ripTriggerUpdate.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            RemoveUnreachableRoutesFromRoutingTable();
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }

    }

    public static void main(String[] args) {

    }
}
