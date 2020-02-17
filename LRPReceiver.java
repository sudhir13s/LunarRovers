import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LRPReceiver implements Runnable {
    private final static int SYN = 1;
    private final static int ACK = 2;
    private final static int FIN = 3;
    //    private final static int RST = 5;
    private final static int TIMEOUT = 0;
    private static int SEQ = 1000;
    private static int ACK_RECEIVED = 0;
    private final static int UDP_PACKET_SIZE = 65507; //65,507

    private DatagramSocket socket;
    private InetAddress source;
    private InetAddress destination;
    private int port;
    private String fileName;
    //    private static int incrementFileNumber;
    private static boolean isFileOutputStream;

    LRPReceiver(InetAddress source, int port, DatagramSocket socket) {
        this.port = port;
        this.socket = socket;
//        this.source = source;
        this.fileName = "LunarRoverProtocolFile";
//        incrementFileNumber = 0;
        isFileOutputStream = true;
    }

    private FileOutputStream openFile() throws FileNotFoundException {
        File file = new File(this.fileName);
//        incrementFileNumber++;
        isFileOutputStream = true;
        return new FileOutputStream(file, false);
    }

    private void closeFile(FileOutputStream fileOutputStream) throws IOException {
        fileOutputStream.close();
        SEQ = 1000;
        ACK_RECEIVED = 0;
        isFileOutputStream = false;
    }

    private void ReceiveLRPPacket() throws IOException {
        FileOutputStream fileOutputStream = openFile();

        while (true) {
            try {
                byte[] bufferData = new byte[UDP_PACKET_SIZE];
                DatagramPacket receiverPacket = new DatagramPacket(bufferData, bufferData.length);
//                this.socket.setSoTimeout(TIMEOUT);
                this.socket.receive(receiverPacket);
//                System.out.println("Received data.");
//                System.out.println("I'm writing received data " + Arrays.toString(receiverPacket.getData()));

                LRP lrp = processLRP(receiverPacket);
                if (RoutingTable.ROUTE_ENTRIES.containsKey(lrp.DESTINATION_IP)) {
                    NextHopInfoTable nextHopInfo = RoutingTable.ROUTE_ENTRIES.get(lrp.DESTINATION_IP);
                    // if i'm the receiver
                    if (nextHopInfo.nextHopAddress.equals(InetAddress.getLocalHost()) && nextHopInfo.hopCount == 0) {
                        this.source = lrp.DESTINATION_IP;
                        this.destination = lrp.SOURCE_IP;

                        if (!isFileOutputStream) {
                            fileOutputStream = openFile();
                        }
                        System.out.println("RECEIVER -> Received: SEQ: " + lrp.SEQ + " ACK: " + lrp.ACK + " Last ACK_RECEIVED: " + ACK_RECEIVED +
                                " src: " + this.source + " dst " + this.destination);
                        if ((lrp.FLAGS & (1L << SYN)) != 0) {
                            SEQ = 1000;
                            handShake(lrp);
                            SEQ += 1;
                        } else if ((lrp.FLAGS & (1L << FIN)) != 0) {
                            terminate(lrp, fileOutputStream);
                        } else {
                            byte[] data = new byte[0];
                            // in sequence
                            if (ACK_RECEIVED + 1 == lrp.SEQ) {
                                ACK_RECEIVED = lrp.SEQ;
                                fileOutputStream.write(lrp.data);
//                                System.out.println("I'm writing for seq: " + lrp.SEQ + "->" + Arrays.toString(lrp.data));
                                System.out.println("RECEIVER -> Order - Sending SEQ: " + SEQ + " ACK: " + ACK_RECEIVED +
                                        " src: " + this.source + " dst " + this.destination);
                                sendFlagsACK(ACK, SEQ, ACK_RECEIVED, this.source, this.destination, data);
                                SEQ += 1;
                            }
                            // otherwise resend
                            else {
                        System.out.println("RECEIVER -> Not order - Resending SEQ: " + SEQ + " ACK: " + ACK_RECEIVED +
                                " src: " + this.source + " dst " + this.destination);
                                sendFlagsACK(ACK, SEQ, ACK_RECEIVED, this.source, this.destination, data);
                            }
                        }
                    }
                    // otherwise forward the packet
                    else {
                        System.out.println("RECEIVER -> Routing data from " + InetAddress.getLocalHost() + " to " + nextHopInfo.nextHopAddress);
                        DatagramPacket forwardPacket = new DatagramPacket(receiverPacket.getData(), receiverPacket.getLength(), nextHopInfo.nextHopAddress, this.port);
                        this.socket.send(forwardPacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handShake(LRP LRP) throws IOException {
        byte[] data = new byte[0];
        System.out.println("RECEIVER -> Received Handshake SEQ: " + LRP.SEQ + " ACK: " + LRP.ACK +
                " src: " + this.source + " dst " + this.destination);
        sendFlagsACK(SYN, SEQ, LRP.SEQ, this.source, this.destination, data);
    }

    private void terminate(LRP LRP, FileOutputStream fileOutputStream) throws IOException {
        byte[] data = new byte[0];
        System.out.println("RECEIVER -> Received Terminate SEQ: " + LRP.SEQ + " ACK: " + LRP.ACK +
                " src: " + this.source + " dst " + this.destination);
        sendFlagsACK(FIN, SEQ, LRP.SEQ, this.source, this.destination, data);
        closeFile(fileOutputStream);
    }

    private LRP processLRP(DatagramPacket packetReceive) throws UnknownHostException {
        byte[] buffer = packetReceive.getData();
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, buffer.length);
        byte[] data = new byte[packetReceive.getLength()];
        byteBuffer.get(data);
        LRP lrp = new LRP();
        return lrp.LRPDecodeData(data);
    }

    private void sendFlagsACK(int flagbit, int sequence, int ack, InetAddress source, InetAddress destination, byte[] bytes) throws IOException {
        LRP lrp = new LRP();
        byte[] data = lrp.LRPEncodeData(flagbit, sequence, ack, source, destination, bytes);
        // now route.
        if (RoutingTable.ROUTE_ENTRIES.containsKey(destination)) {
            NextHopInfoTable nextHopInfo = RoutingTable.ROUTE_ENTRIES.get(destination);
            DatagramPacket response = new DatagramPacket(data, data.length, nextHopInfo.nextHopAddress, this.port);
            System.out.println("RECEIVER -> Sending SEQ: " + sequence + " ACK: " + ack +
                    " src: " + source + " dst " + destination);
            this.socket.send(response);
            ACK_RECEIVED = ack;
        }
    }

    @Override
    public void run() {
        try {
            ReceiveLRPPacket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        try {
//            LunarRoverProtocol lrp = new LunarRoverProtocol();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        String fileName = "test1.txt";
//
//        LRPReceiver sender = null;
//        sender = new LRPReceiver(LunarRoverProtocol.RDP_PORT, LunarRoverProtocol.SOCKET, "LunarRoverProtocolFile");
//        sender.run();
    }
}
