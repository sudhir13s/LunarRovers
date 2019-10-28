import java.io.IOException;
import java.net.*;

public class RIPReceiver implements Runnable {
    private int port;
//    private DatagramSocket socket;

    static DatagramSocket SOCKET;
    final static int PORT = 5521;

    RIPReceiver() {
        this.port = 5521;
    }
    RIPReceiver(int port, DatagramSocket socket) throws SocketException {
        this.port = port;
//        this.socket = socket;
        SOCKET = new DatagramSocket(PORT);
    }

    private void ReceiveUDPPacket() {
        try {
            int bufferSize = 504;
            byte[] bufferData = new byte[bufferSize];
            DatagramPacket receiverPacket = new DatagramPacket(bufferData, bufferData.length);
            SOCKET.receive(receiverPacket);

//            System.out.println("RIPReceiver: Received UDP message.");
            byte[] data = receiverPacket.getData();
            RIPDataProcessor ripDataProcessor = new RIPDataProcessor(data, receiverPacket.getAddress());
            Thread ripDataProcessorThread = new Thread(ripDataProcessor, "RIP Data Processor");
            ripDataProcessorThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
//        System.out.println("RIPReceiver: RIP Receiver started");
        while (true) {
            ReceiveUDPPacket();
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
//        RIPMulticastReceiver server = null;
//        try {
//            server = new RIPMulticastReceiver();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
//        assert server != null;
//        server.ReceiveRIPData();
    }
}
