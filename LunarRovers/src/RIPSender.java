import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;

public class RIPSender implements Runnable {

    DatagramSocket clientSocket;
    final int PORT = 6520;
    final int BUFFER_SIZE = 504;
    byte[] buffer;

    public RIPSender() {

        try {
            clientSocket = new DatagramSocket(PORT);
            buffer = new byte[BUFFER_SIZE];
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void sendRoutingTableToNeighbors() {

//        try(final DatagramSocket socket = new DatagramSocket()) {
//            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//        }
        DatagramPacket packet;
        NextHopInfoTable nextHopeInfo;
        byte[] bytes;
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        byteBuffer.clear();

        byteBuffer.put(LunarRover.rip.RIPEncodeHeader());
        for (InetAddress neighbor : LunarRover.neighbors) {

            try {
                for (Map.Entry<InetAddress, NextHopInfoTable> route : LunarRover.routingTable.getRoutingTable().entrySet()) {
                    nextHopeInfo = route.getValue();

                    byteBuffer.put(setRIPProtocolInfo(route.getKey(), nextHopeInfo));

                }
                byteBuffer.flip();
                bytes = new byte[byteBuffer.remaining()];
                byteBuffer.get(bytes);

                packet = new DatagramPacket(bytes, BUFFER_SIZE, neighbor, PORT);
                clientSocket.send(packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }





    }

    public byte[] setRIPProtocolInfo(InetAddress destinationAddress, NextHopInfoTable nextHopeInfo) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(destinationAddress.getAddress());
            outputStream.write(nextHopeInfo.subnetMask.getAddress());
            outputStream.write(nextHopeInfo.neighborAddress.getAddress());
            outputStream.write(nextHopeInfo.hopCount.byteValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }

    @Override
    public void run() {
        System.out.println("RIP sender started");

        sendRoutingTableToNeighbors();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        RIPSender client = new RIPSender();

    }
}
