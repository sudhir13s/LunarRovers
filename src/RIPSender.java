import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;

public class RIPSender implements Runnable {

//    static DatagramSocket socket;
//    final static int PORT = 6520;
    final static int BUFFER_SIZE = 504;
//    byte[] buffer;

    public RIPSender() {

//        try {
//            socket = new DatagramSocket(PORT);
////            buffer = new byte[BUFFER_SIZE];
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static synchronized void sendRoutingTableToNeighbors() {
            DatagramPacket packet;
            NextHopInfoTable nextHopeInfo;
//            byte[] bytes;
//            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            byte[] bytes = new byte[BUFFER_SIZE];
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.clear();

            byteBuffer.put(LunarRover.rip.RIPEncodeHeader());
            for (InetAddress neighbor : LunarRover.neighbors) {
                try {
                    for (Map.Entry<InetAddress, NextHopInfoTable> route : LunarRover.routingTable.getRoutingTable().entrySet()) {
                        nextHopeInfo = route.getValue();
                        byteBuffer.put(LunarRover.rip.RIPEncodeHeaderData(route.getKey(), nextHopeInfo.subnetMask,
                                nextHopeInfo.neighborAddress, nextHopeInfo.hopCount));
                        //byteBuffer.put(setRIPProtocolInfo(route.getKey(), nextHopeInfo));
                        System.out.println("Sender: Neighbor entry: " + neighbor.toString());
                    }
                    byteBuffer.flip();
                    bytes = new byte[byteBuffer.remaining()];
                    byteBuffer.get(bytes);

                    packet = new DatagramPacket(bytes, bytes.length, neighbor, RIPReceiver.PORT);
                    RIPReceiver.socket.send(packet);
                    System.out.println("Sender: sent routing table.");
                    byteBuffer.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

    }

//    public static byte[] setRIPProtocolInfo(InetAddress destinationAddress, NextHopInfoTable nextHopeInfo) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        try {
//            outputStream.write(destinationAddress.getAddress());
//            outputStream.write(nextHopeInfo.subnetMask.getAddress());
//            outputStream.write(nextHopeInfo.neighborAddress.getAddress());
//            outputStream.write(nextHopeInfo.hopCount.byteValue());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return outputStream.toByteArray();
//    }

    @Override
    public void run() {
        System.out.println("RIP sender started");

        while (true) {
            try {
                System.out.println("Sender: Call sendRoutingTableToNeighbors");
                sendRoutingTableToNeighbors();
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        RoutingTable rt = new RoutingTable();
        LunarRover lr = new LunarRover();
        try {
            RoutingTable.routeEntries.put(InetAddress.getByName("192.168.56.3"),
                    new NextHopInfoTable(InetAddress.getByName("255.255.255.0"),
                            InetAddress.getByName("192.168.56.3"), 1));
            LunarRover.neighbors.add(InetAddress.getByName("192.168.56.3"));
//            routeEntries.put(InetAddress.getByName("192.168.1.2"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//            routeEntries.put(InetAddress.getByName("192.168.1.3"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
//            routeEntries.put(InetAddress.getByName("192.168.1.4"), new NextHopInfoTable(InetAddress.getByName("255.255.255.0"), InetAddress.getByName("192.168.1.0"), 1));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
//        RIPSender client = new RIPSender();
        sendRoutingTableToNeighbors();

    }
}
