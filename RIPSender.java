import java.io.IOException;
import java.net.*;
import java.util.Map;

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
//        synchronized (RoutingTable.routeEntriesLock) {
        for (Map.Entry<InetAddress, InetAddress> neighbor : LunarRover.NEIGHBORS_ENTRIES.entrySet()) {
            RIP rip = new RIP();
            for (Map.Entry<InetAddress, NextHopInfoTable> route : RoutingTable.ROUTE_ENTRIES.entrySet()) {
                NextHopInfoTable nextHopeInfo = route.getValue();
                // implement split horizon with poison reverse.
                Integer hopCount = nextHopeInfo.hopCount;
                if (neighbor.getValue().equals(nextHopeInfo.nextHopAddress) && hopCount > 1) {
                    hopCount = 16;
                }
                // add RIP entry
                RIPEntry ripEntry = new RIPEntry(route.getKey(), nextHopeInfo.subnetMask, nextHopeInfo.nextHopAddress, hopCount);
                rip.ripEntries.add(ripEntry);
            }
            // Packet setup
            byte[] data = rip.RIPEncodeData();
            DatagramPacket packet = new DatagramPacket(data, data.length, neighbor.getValue(), RIPReceiver.PORT);
            RIPReceiver.SOCKET.send(packet);
        }
//    }
        RoutingTable rt = new RoutingTable();
//        rt.PrintRoutingTable();
    }

    @Override
    public void run() {
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

    }
}
