import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

public class RIPTriggerUpdate implements Runnable {

    private InetAddress multicastAddress;
    private int port = 6520;
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
        if (RoutingTable.isTriggerUpdate) {
            // get multicast group
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = this.multicastAddress;
            DatagramPacket packet;
            NextHopInfoTable nextHopeInfo;

            synchronized (RoutingTable.routeEntriesLock) {
                RIP rip = new RIP();
                for (Map.Entry<InetAddress, NextHopInfoTable> route : RoutingTable.routeEntries.entrySet()) {
                    nextHopeInfo = route.getValue();
                    // add RIP entry which has metricChanged flag set to true.
                    if (nextHopeInfo.metricChanged) {
                        // implement split horizon with poison reverse.
                        Integer hopCount = nextHopeInfo.hopCount;
                        if (hopCount > 1) {
                            hopCount = 16;
                        }
                        RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, hopCount);
                        rip.ripEntries.add(ripEntry);
                        // change the metricChanged flag and update.
                        nextHopeInfo.metricChanged = false;
                        RoutingTable.routeEntries.put(route.getKey(), nextHopeInfo);
                    }
                }
                // Packet setup
                byte[] data = rip.RIPEncodeData();
                packet = new DatagramPacket(data, data.length, group, this.port);
                // let 'er rip
                socket.send(packet);
                RoutingTable.isTriggerUpdate = false;
                System.out.println("RIPTriggerUpdate: Trigger update sent.");
            }
        }
    }

    @Override
    public void run() {
        System.out.println("RIPTriggerUpdate: RIP Trigger update called.");
        try {
            SendTriggerUpdate();
        } catch (IOException e) {
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
