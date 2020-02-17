import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LRPSender implements Runnable {
    private final static int SYN = 1;
    private final static int ACK = 2;
    private final static int FIN = 3;
    //    private final static int RST = 4;
    private final static int TIMEOUT = 500;
    static int SEQ = 100;
    static int ACK_RECEIVED = 0;
    final static int PACKET_SIZE = 65490; //65,507

    private InetAddress source;
    private InetAddress destination;
    private int port;
    private DatagramSocket socket;
    private String fileName;

    LRPSender(InetAddress source, InetAddress destination, int port, DatagramSocket socket, String fileName) throws UnknownHostException {
        this.port = port;
        this.socket = socket;
        this.source = source;
        this.destination = destination;
        this.fileName = fileName;
    }

    private void SendLRPPacket() throws IOException, InterruptedException {
        boolean flag;
        int counter = 0;
        do {
            flag = handShake();
            counter++;
            // wait for 500ms for next try.
            Thread.sleep(500);
        } while (!flag && counter < 10);

        if (counter >= 10) {
            System.err.println("SENDER -> Handshake timeout.");
            return;
        }

        try (FileInputStream fis = new FileInputStream(this.fileName)) {
            byte[] buffer = new byte[PACKET_SIZE];
            int len = 0;

            while ((len = fis.read(buffer)) > 0) {
                if (len < PACKET_SIZE) {
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, buffer.length);
                    byte[] bytes = new byte[len];
                    byteBuffer.get(bytes);
                    buffer = bytes;
                }

//                System.out.println("I'm writing sending data " + Arrays.toString(buffer));

                SEQ += 1;
                sendPacket(ACK, SEQ, ACK_RECEIVED, this.source, this.destination, buffer);

                counter = 0;
                boolean ackReceived = false;
                while (!ackReceived && counter < 10) {
                    counter += 1;
                    LRP lrp = receiveACKs();
                    if (lrp != null && lrp.ACK != 0 && SEQ == lrp.ACK) {
                        ackReceived = true;
                        ACK_RECEIVED = lrp.SEQ;
                    } else {
                        sendPacket(ACK, SEQ, ACK_RECEIVED, this.source, this.destination, buffer);
                    }
                }
                if (counter >= 10) {
                    System.out.println("SENDER -> Destination not reachable");
                    break;
                }
            }
            if (counter < 10) {
                SEQ += 1;
                terminate();
            }
        }
    }

    private boolean handShake() throws IOException {
        byte[] data = new byte[0];
        int ACK = 0;
        System.out.println("SENDER -> Sending Handshake SEQ: " + SEQ + " ACK: " + ACK + ": " + this.source + ": " + this.destination);
        System.out.println(Arrays.toString(data));
        sendPacket(SYN, SEQ, ACK, this.source, this.destination, data);
        return receiveFlags(SYN);
    }

    private void terminate() throws IOException {
        byte[] data = new byte[0];
        System.out.println("SENDER -> Sending Terminate SEQ: " + SEQ + " ACK: " + ACK_RECEIVED + ": " + this.source + ": " + this.destination);
        sendPacket(FIN, SEQ, ACK_RECEIVED, this.source, this.destination, data);
        receiveFlags(FIN);
    }

    private void sendPacket(int flagbit, int sequence, int ack, InetAddress source, InetAddress destination, byte[] bytes) throws IOException {
        LRP lrp = new LRP();
        byte[] data = lrp.LRPEncodeData(flagbit, sequence, ack, source, destination, bytes);
        // now route.
        if (RoutingTable.ROUTE_ENTRIES.containsKey(destination)) {
            NextHopInfoTable nextHopInfo = RoutingTable.ROUTE_ENTRIES.get(destination);
            DatagramPacket packetSend = new DatagramPacket(data, data.length, nextHopInfo.nextHopAddress, this.port);
            this.socket.send(packetSend);
            System.out.println("SENDER -> Sending SEQ: " + sequence + " ACK: " + ack + " Last ACK_RECEIVED: " + ACK_RECEIVED + ": " + this.source + ": " + this.destination);
        }
        // else do nothing. it will time out.
    }

    private LRP receiveACKs() throws IOException {
        byte[] ackBytes = new byte[LRP.HEADER_SIZE];
        DatagramPacket packetReceive = new DatagramPacket(ackBytes, ackBytes.length);

        LRP lrp;
        try {
            this.socket.setSoTimeout(TIMEOUT);
            this.socket.receive(packetReceive);
            lrp = new LRP();
            lrp.LRPDecodeData(packetReceive.getData());
            System.out.println("SENDER -> Received SEQ: " + lrp.SEQ + " ACK: " + lrp.ACK + " Last ACK_RECEIVED: " + ACK_RECEIVED + ": " + this.source + ": " + this.destination);
        } catch (SocketTimeoutException e) {
            lrp = new LRP();
        }
        return lrp;
    }

    private boolean receiveFlags(int flagbit) throws IOException {
        byte[] ackBytes = new byte[LRP.HEADER_SIZE];
        DatagramPacket packetReceive = new DatagramPacket(ackBytes, ackBytes.length);
        try {
            this.socket.setSoTimeout(TIMEOUT);
            this.socket.receive(packetReceive);
        } catch (SocketTimeoutException e) {
            return false;
        }

        LRP lrp = new LRP();
        lrp = lrp.LRPDecodeData(packetReceive.getData());
        System.out.println("SENDER -> Received flags SEQ: " + lrp.SEQ + " ACK: " + lrp.ACK + " Last ACK_RECEIVED: " + ACK_RECEIVED + ": " + this.source + ": " + this.destination);

        // bits
        if ((lrp.FLAGS & (1L << SYN)) != 0 && SYN == flagbit) {
            ACK_RECEIVED = lrp.SEQ;
            return SEQ == lrp.ACK;

        } else if ((lrp.FLAGS & (1L << FIN)) != 0 && FIN == flagbit) {
            ACK_RECEIVED = lrp.SEQ;
            return SEQ == lrp.ACK;
        } else return false;
    }

    @Override
    public void run() {
        try {
            SendLRPPacket();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        try {
//            LunarRoverProtocol lrp = new LunarRoverProtocol();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        String fileName = "test.txt";
//
//        try {
//            LRPSender sender = new LRPSender(InetAddress.getByName("192.168.56.3"), LunarRoverProtocol.RDP_PORT, LunarRoverProtocol.SOCKET, fileName);
//            sender.run();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
    }
}
