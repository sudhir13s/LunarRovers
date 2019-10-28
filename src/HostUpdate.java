import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

public class HostUpdate implements Runnable {

    private String[] hosts;
    private InetAddress nextHopAddress;
    private InetAddress subnetMask;
    String hostsAddress = "10.0.0.0";
//    boolean isTriggerUpdate = false;

    HostUpdate(String[] hosts, InetAddress nextHopAddress, InetAddress subnetMask) {
        this.hosts = hosts;
        this.nextHopAddress = nextHopAddress;
//        this.subnetMask = subnetMask;
//        this.isTriggerUpdate = false;
        try {
            this.subnetMask = InetAddress.getByName("255.255.255.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String UpdateMapping(InetAddress nextHopAddress, String nodeNum) {
//        String newAddress = "";
//        newAddress = nextHopAddress.getHostAddress();
//        newAddress = newAddress.replace(newAddress.substring(0, newAddress.indexOf(".")), "10");
//        newAddress = newAddress.replace(newAddress.substring(newAddress.lastIndexOf("."), newAddress.length()), nodeNum);

        String[] newAddress = new String[4];
        newAddress[0] = "10";
        newAddress[1] = "0";
        newAddress[2] = nodeNum;
        newAddress[3] = "0";
        String address = Arrays.toString(newAddress);
        address = address.substring(1, address.length() - 1).replace(", ", ".");
        return address;
    }

    private void HostEntriesMapping() {
        for (String host : this.hosts) {
            try {
                InetAddress destinationAddress = InetAddress.getByName(UpdateMapping(this.nextHopAddress, host));
                LunarRover.MAPPING.put(destinationAddress, this.nextHopAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void UpdateHostEntriesInRoutingTable() {
//        boolean isTriggerUpdate = false;
        if (RoutingTable.routeEntries.isEmpty()) {
            boolean isRouteChanged = false;
            RoutingTable rt = new RoutingTable();
            for (Map.Entry<InetAddress, InetAddress> map : LunarRover.MAPPING.entrySet()) {
                Integer interfaceId = 0;
                Integer hopCount = 0;
                NextHopInfoTable nextHopInfo = new NextHopInfoTable(this.subnetMask, map.getValue(), interfaceId, hopCount);
                nextHopInfo.metricChanged = true;
//                System.out.println("HostUpdate: calling updateRoutingTable");

                RoutingTable.routeEntries.put(map.getKey(), nextHopInfo);
                isRouteChanged = true;

//                if (rt.updateRoutingTable(map.getKey(), nextHopInfo)) {
//
//                }
            }
            // if route table updated.
            if (isRouteChanged) {
//                System.out.println("HostUpdate: Routing table updated.");
                rt.PrintRoutingTable();
                // if RoutingTable.isTriggerUpdate flag is set, send trigger update.
//                System.out.println("HostUpdate: RIP Trigger update called.");
                RIPTriggerUpdate.isTriggerUpdate = true;
                System.out.println("Starting Trigger update...");
                Thread ripTriggerUpdate = new Thread(new RIPTriggerUpdate(LunarRover.MULTICAST_ADDRESS, LunarRover.MULTICAST_PORT), "RIP Trigger Update");
                ripTriggerUpdate.start();
            }
        }
    }

    // implemented Runnable, but will not create thread. normal run() call.
    @Override
    public void run() {
        try {
            HostEntriesMapping();
            UpdateHostEntriesInRoutingTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String hosts[] = "1 2".split(" ");
        InetAddress localhost = null;
        try {
            localhost = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        HostUpdate hostUpdate = new HostUpdate(hosts, localhost, localhost);
        hostUpdate.run();

        GarbageCollector gc = new GarbageCollector();
        gc.run();
    }
}
