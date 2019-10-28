import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class RIPTriggerUpdate implements Runnable {

    private InetAddress multicastAddress;
    private int port = 6520;

    volatile static boolean isTriggerUpdate = false;
//    private final static int BUFFER_SIZE = 504;

    public RIPTriggerUpdate(int nodeNum) throws UnknownHostException {
        this(InetAddress.getByName("230.230.230.230"), 6520);
    }

    public RIPTriggerUpdate(String multicastIp, int port) {
        try {
            this.multicastAddress = InetAddress.getByName(multicastIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
    }

    public RIPTriggerUpdate(InetAddress multicastIp, int port) {
        this.multicastAddress = multicastIp;
        this.port = port;
    }

    private synchronized void SendTriggerUpdate() throws IOException {
        // if RoutingTable.isTriggerUpdate flag is set, send trigger update.
        // get multicast group

        // initial multicast. keep triggering.
        if (RoutingTable.routeEntries.size() <= 1 || isTriggerUpdate) {
//            System.out.println("Multicast Trigger updated.");
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = this.multicastAddress;
            DatagramPacket packet;
            RoutingTable rt = new RoutingTable();
            NextHopInfoTable nextHopeInfo;

//            synchronized (RoutingTable.routeEntriesLock) {
            RIP rip = new RIP();
            for (Map.Entry<InetAddress, NextHopInfoTable> route : RoutingTable.routeEntries.entrySet()) {
                nextHopeInfo = route.getValue();

                // add RIP entry which has metricChanged flag set to true.
                if (nextHopeInfo.metricChanged) {
                    // implement split horizon with poison reverse.
                    Integer hopCount = nextHopeInfo.hopCount;
                    if (LunarRover.MAPPING.containsValue(nextHopeInfo.nextHopAddress) && hopCount > 1) {
                        hopCount = 16;
                    }
                    RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, hopCount);
                    rip.ripEntries.add(ripEntry);
                    // change the metricChanged flag and update.
                    nextHopeInfo.metricChanged = false;
//                rt.updateRoutingTable(route.getKey(), nextHopeInfo);
                    RoutingTable.routeEntries.put(route.getKey(), nextHopeInfo);

                    isTriggerUpdate = false;
//                    System.out.println("RIPTriggerUpdate: isTriggerUpdate - " + isTriggerUpdate);
                } else if (RoutingTable.routeEntries.size() <= 1) {
                    RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, nextHopeInfo.hopCount);
                    rip.ripEntries.add(ripEntry);
                }
            }
            // Packet setup
            byte[] data = rip.RIPEncodeData();
            packet = new DatagramPacket(data, data.length, group, this.port);
            // let 'er rip
            socket.send(packet);

        }
    }

    @Override
    public void run() {
//        System.out.println("RIPTriggerUpdate: RIP Trigger update started.");
        while (true)
            try {
                SendTriggerUpdate();
                Thread.sleep(5000);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
    }


    public static void main(String[] args) {
//        RoutingTable rt = new RoutingTable();
//        LunarRover lr = new LunarRover();
//        try {
//            RoutingTable.routeEntries.put(InetAddress.getByName("192.168.56.3"),
//                    new NextHopInfoTable(InetAddress.getByName("255.255.255.0"),
//                            InetAddress.getByName("192.168.56.3"), 1));
//            RoutingTable.neighbors.add(InetAddress.getByName("192.168.56.3"));
////            routeEntries.put(InetAddress.getByName("192.168.1.2"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
////            routeEntries.put(InetAddress.getByName("192.168.1.3"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
////            routeEntries.put(InetAddress.getByName("192.168.1.4"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
////        RIPSender client = new RIPSender();
//        sendRoutingTableToNeighbors();

    }
}
