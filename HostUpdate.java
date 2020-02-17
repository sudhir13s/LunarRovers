import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class HostUpdate implements Runnable {
    private String host;
    private InetAddress nextHopAddress;
    private InetAddress subnetMask;

    HostUpdate(String host, InetAddress nextHopAddress, InetAddress subnetMask) {
        this.host = host;
        this.nextHopAddress = nextHopAddress;
        try {
            this.subnetMask = InetAddress.getByName("255.255.255.0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private String UpdateMapping(String nodeNum) {
        String[] newAddress = new String[4];
        newAddress[0] = "10";
        newAddress[1] = "0";
        newAddress[2] = nodeNum;
        newAddress[3] = "0";
        String address = Arrays.toString(newAddress);
        address = address.substring(1, address.length() - 1).replace(", ", ".");
        return address;
    }

//    private void HostEntriesMapping() {
//        for (String host : this.host) {
//            try {
//                InetAddress destinationAddress = InetAddress.getByName(UpdateMapping(host));
//                LunarRover.NEIGHBORS_ENTRIES.put(destinationAddress, this.nextHopAddress);
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private synchronized void UpdateHostEntriesInRoutingTable() {
        if (RoutingTable.ROUTE_ENTRIES.isEmpty()) {
            boolean isRouteChanged = false;
            RoutingTable rt = new RoutingTable();
//            for (String host : this.host) {
            try {
                InetAddress destinationAddress = InetAddress.getByName(UpdateMapping(host));
                LunarRover.source_ip = destinationAddress;
                Integer interfaceId = RoutingTable.InterfaceID++;
                Integer hopCount = 0;
                NextHopInfoTable nextHopInfo = new NextHopInfoTable(this.subnetMask, this.nextHopAddress, interfaceId, hopCount);
                nextHopInfo.metricChanged = true;
                RoutingTable.ROUTE_ENTRIES.put(destinationAddress, nextHopInfo);
                isRouteChanged = true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
//            }
            // if route table updated. if RoutingTable.isTriggerUpdate flag is set, send trigger update.
            if (isRouteChanged) {
//                rt.PrintRoutingTable();
                RIPTriggerUpdate.isTriggerUpdate = true;
                System.out.println("Starting Trigger update...");
                Thread ripTriggerUpdate = new Thread(new RIPTriggerUpdate(LunarRover.RIP_MULTICAST_ADDRESS, LunarRover.RIP_MULTICAST_PORT), "RIP Trigger Update");
                ripTriggerUpdate.start();
            }
        }
    }

    // implemented Runnable, but will not create thread. normal run() call.
    @Override
    public void run() {
        try {
            UpdateHostEntriesInRoutingTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String hosts = "1";
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
