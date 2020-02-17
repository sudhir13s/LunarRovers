import java.net.InetAddress;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

public class GarbageCollector implements Runnable {
    private final int time_to_live = 6; // 5 secs

    private void RemoveUnreachableRoutesFromRoutingTable() {
        NextHopInfoTable nextHopeInfo;
        boolean isTriggerUpdate = false;
        RoutingTable rt = new RoutingTable();

        Iterator<Map.Entry<InetAddress, NextHopInfoTable>> iterator = RoutingTable.ROUTE_ENTRIES.entrySet().iterator();
//        synchronized (RoutingTable.routeEntriesLock) {
            while (iterator.hasNext()) {
                Map.Entry<InetAddress, NextHopInfoTable> route = iterator.next();
                nextHopeInfo = route.getValue();
                Instant instant = Instant.now();
                // Rover self entry or subnet entries. don't remove, rather update time.
                if (nextHopeInfo.hopCount == 0) {
                    nextHopeInfo.ttl = instant.getEpochSecond();
                    RoutingTable.ROUTE_ENTRIES.put(route.getKey(), nextHopeInfo);
                }
                else if (nextHopeInfo.hopCount > 0) {
                    // means the route is not reachable. update expire entry or remove it.
                    if (nextHopeInfo.ttl == 0 && nextHopeInfo.hopCount == 16 && !nextHopeInfo.metricChanged) {
//                        System.out.println("GarbageCollector: Removing entry: " + route.getKey() + " : " + route.getValue().nextHopAddress + " : " + route.getValue().hopCount);
                        iterator.remove();
                        // don't remove neighbors entry. Keep it. they are being blocked by IP.
                        // removing neighbor will cause issue,
                        // that they are not in neighbor table and multicast updates will have routes-to data (not direct reachable).
                    } else if ((instant.getEpochSecond() - nextHopeInfo.ttl > this.time_to_live) && (nextHopeInfo.hopCount != 16)) {
                        nextHopeInfo.ttl = 0;
                        nextHopeInfo.hopCount = 16;
                        nextHopeInfo.metricChanged = true;
                        RoutingTable.ROUTE_ENTRIES.put(route.getKey(), nextHopeInfo);
                        isTriggerUpdate = true;
                    }
                }
            }
//        }
        // if RoutingTable.isTriggerUpdate flag is set, send trigger update.
        if (isTriggerUpdate) {
//            rt.PrintRoutingTable();
            RIPTriggerUpdate.isTriggerUpdate = true;
            isTriggerUpdate = false;
        }
    }

    @Override
    public void run() {
        while (true) {
            RemoveUnreachableRoutesFromRoutingTable();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {

    }
}
