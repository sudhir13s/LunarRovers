import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

public class RIPSender implements Runnable {

    private InetAddress multicastAddress;
    private int port = 5520;
    private int nodeNum;

    //    static DatagramSocket socket;
//    final static int PORT = 6520;
//    private final static int BUFFER_SIZE = 504;
//    byte[] buffer;

    public RIPSender(int nodeNum) throws UnknownHostException {
        this(InetAddress.getByName("230.230.230.230"), 5520, nodeNum);
    }

    public RIPSender(String multicastIp, int port, int nodeNum) {
        try {
            this.multicastAddress = InetAddress.getByName(multicastIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port = port;
        this.nodeNum = nodeNum;
    }

    public RIPSender(InetAddress multicastIp, int port, int nodeNum) {
        this.multicastAddress = multicastIp;
        this.port = port;
        this.nodeNum = nodeNum;
    }

    private synchronized void SendUDPPacket() throws IOException {
        // get multicast group
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = this.multicastAddress;
        DatagramPacket packet;
        NextHopInfoTable nextHopeInfo;

        synchronized (RoutingTable.routeEntriesLock) {
            RIP rip = new RIP();
            for (Map.Entry<InetAddress, NextHopInfoTable> route : RoutingTable.routeEntries.entrySet()) {
                nextHopeInfo = route.getValue();
                // implement split horizon with poison reverse.
                Integer hopCount = nextHopeInfo.hopCount;
                if (hopCount > 1) {
                    hopCount = 16;
                }
                // add RIP entry
                RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, hopCount);
                rip.ripEntries.add(ripEntry);
            }
            // Packet setup
            byte[] data = rip.RIPEncodeData();
            packet = new DatagramPacket(data, data.length, group, this.port);
            // let 'er rip
            socket.send(packet);
//            System.out.println("RIPSender: UDP packet sent.");
        }
    }

    @Override
    public void run() {
        System.out.println("RIPSender: RIP sender started");

        while (true) {
            try {
//                System.out.println("Sender: Call sendRoutingTable");
                SendUDPPacket();
                Thread.sleep(5000);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
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
