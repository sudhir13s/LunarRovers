import java.io.IOException;
import java.net.*;

public class RIPMulticastReceiver implements Runnable {
    private static MulticastSocket multicastSocket;
    private InetAddress multicastAddress;
    private int multicastPort = 5520;
    private final int BUFFER_SIZE = 504;

    RIPMulticastReceiver() throws UnknownHostException {
        this(InetAddress.getByName("224.0.0.9"), 5520);
    }

    RIPMulticastReceiver(String multicastIp, int port) {
        try {
            this.multicastAddress = InetAddress.getByName(multicastIp);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.multicastPort = port;
    }

    private RIPMulticastReceiver(InetAddress multicastIp, int port) {
        this.multicastAddress = multicastIp;
        this.multicastPort = port;
    }

    private void JoinMulticastGroup() throws IOException {
        multicastSocket = new MulticastSocket(this.multicastPort);
        multicastSocket.joinGroup(this.multicastAddress);
    }

    private void ReceiveRIPData() throws InterruptedException {
        while (true) {
            try {
                byte[] bufferData = new byte[this.BUFFER_SIZE];
//                byte[] bufferData = hexStringToByteArray("0202000000020001c0a83803ffffff00c0a8380300000001c0a83804ffffff00c0a8380400000001");
                DatagramPacket receiverPacket = new DatagramPacket(bufferData, bufferData.length);
                multicastSocket.receive(receiverPacket);
                // if not a self multicast.
                if (!receiverPacket.getAddress().equals(InetAddress.getLocalHost())) {
//                    System.out.println("RIPMulticastReceiver: Received Multicast from : " + receiverPacket.getAddress());
                    byte[] data = receiverPacket.getData();
                    RIPDataProcessor ripDataProcessor = new RIPDataProcessor(data, receiverPacket.getAddress());
                    Thread ripDataProcessorThread = new Thread(ripDataProcessor, "RIP Data Processor");
                    ripDataProcessorThread.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Thread.sleep(100);
        }
    }

    private void LeaveMulticastGroup() throws IOException {
        //close up ship
        multicastSocket.leaveGroup(this.multicastAddress);
        multicastSocket.close();
    }

    private void ReceiveMulticastPacket() throws IOException {
        //join to multicast group.
        JoinMulticastGroup();
        //receive data.
        try {
            ReceiveRIPData();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //leave multicast group.
        LeaveMulticastGroup();
    }

    @Override
    public void run() {
        try {
            ReceiveMulticastPacket();
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

    }
}
