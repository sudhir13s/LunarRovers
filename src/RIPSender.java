import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.function.LongUnaryOperator;

public class RIPSender implements Runnable {
    private int port;
    private DatagramSocket socket;

    RIPSender() {
        this.port = 5521;
    }

    RIPSender(int port, DatagramSocket socket) {
        this.port = port;
        this.socket = socket;
    }

    public void SendUDPPacket() throws IOException {
        // get socket
//        LunarRover.socket = new DatagramSocket();
//        synchronized (RoutingTable.routeEntriesLock) {
        for (Map.Entry<InetAddress, InetAddress> neighbors : LunarRover.MAPPING.entrySet()) {
            RIP rip = new RIP();
            for (Map.Entry<InetAddress, NextHopInfoTable> route : RoutingTable.routeEntries.entrySet()) {
                NextHopInfoTable nextHopeInfo = route.getValue();
                // implement split horizon with poison reverse.
                Integer hopCount = nextHopeInfo.hopCount;
                if (neighbors.getValue().equals(nextHopeInfo.nextHopAddress) && hopCount > 1) {
                    hopCount = 16;
                }
                // add RIP entry
                RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, hopCount);
                rip.ripEntries.add(ripEntry);
            }
            // Packet setup
            byte[] data = rip.RIPEncodeData();
            DatagramPacket packet = new DatagramPacket(data, data.length, neighbors.getValue(), RIPReceiver.PORT);
            // let 'er rip
            RIPReceiver.SOCKET.send(packet);
        }
//        System.out.println("RIPSender: 10 sec - send update.");
        RoutingTable rt = new RoutingTable();
        rt.PrintRoutingTable();
//        }
    }

    @Override
    public void run() {
//        System.out.println("RIPSender: RIP sender started");
        while (true) {
            try {
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
//
////        RIPReceiver rr = new RIPReceiver(LunarRover.RIP_PORT, LunarRover.socket);
//
//        rr.ReceiveUDPPacket();
//
//        RIPSender rs = new RIPSender(LunarRover.RIP_PORT, LunarRover.socket);
//        try {
//            rs.SendUDPPacket();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
