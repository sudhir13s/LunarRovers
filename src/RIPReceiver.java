import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RIPReceiver implements Runnable {

    static MulticastSocket multicastSocket;
    private InetAddress multicastAddress;
    private int multicastPort = 6520;

    RIPReceiver() throws UnknownHostException {
        this(InetAddress.getByName("230.230.230.230"), 6520);
    }

    RIPReceiver(String multicastIp, int port) {
        try {
            this.multicastAddress = InetAddress.getByName(multicastIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.multicastPort = port;
    }

    private RIPReceiver(InetAddress multicastIp, int port) {
        this.multicastAddress = multicastIp;
        this.multicastPort = port;
    }

    private void JoinMulticastGroup() throws IOException {
        multicastSocket = new MulticastSocket(this.multicastPort);
        multicastSocket.joinGroup(this.multicastAddress);
        System.out.println("RIPReceiver: Joined multicast group on port " + this.multicastPort);
    }

    private void ReceiveRIPData() {
        // any condition??
        while (true) {
            try {
                int bufferSize = 504;
                byte[] bufferData = new byte[bufferSize];
//                byte[] bufferData = hexStringToByteArray("0202000000020001c0a83803ffffff00c0a8380300000001c0a83804ffffff00c0a8380400000001");
                DatagramPacket receiverPacket = new DatagramPacket(bufferData, bufferData.length);
                multicastSocket.receive(receiverPacket);
                System.out.println("RIPReceiver: received packets***");

                byte[] data = receiverPacket.getData();
                RIPDataProcessor ripDataProcessor = new RIPDataProcessor(data);
                Thread ripDataProcessorThread = new Thread(ripDataProcessor, "RIP Data Processor");
                ripDataProcessorThread.start();

                // give us a way out if needed
//                if("EXIT".equals(msg)) {
//                    System.out.println("No more messages. Exiting : "+msg);
//                    break;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void LeaveMulticastGroup() throws IOException {
        //close up ship
        multicastSocket.leaveGroup(this.multicastAddress);
        multicastSocket.close();
    }

    private void ReceiveUDPPacket() throws IOException {
        //join to multicast group.
        JoinMulticastGroup();
        //receive data.
        ReceiveRIPData();
        //leave multicast group.
        LeaveMulticastGroup();
    }

    @Override
    public void run() {
        System.out.println("RIPReceiver: RIP Receiver started");
        try {
            ReceiveUDPPacket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void main(String[] args) {
        RIPReceiver server = null;
        try {
            server = new RIPReceiver();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert server != null;
        server.ReceiveRIPData();
    }
}
