import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RIPReceiver implements Runnable {

    static DatagramSocket socket;
    final static int PORT = 6520;

    DatagramPacket receiverPacket;
    final int BUFFER_SIZE = 504;

    public RIPReceiver() {

//        try {
//            listenerSocket = new DatagramSocket(PORT);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void run() {
        System.out.println("RIP Receiver started");

        try {
            socket = new DatagramSocket(PORT);
            System.out.println("Receiver: Opened socket on port " + PORT);

            while (true) {
                try {
                    byte[] bufferData = new byte[BUFFER_SIZE];
                    receiverPacket = new DatagramPacket(bufferData, bufferData.length);
                    System.out.println("Receiver: receive packet.");
                    socket.receive(receiverPacket);
                    System.out.println("Receiver: received packet***");

                    byte[] data = receiverPacket.getData();

                    // data received. Now read serialize routing table
                    LunarRover.rip.RIPDecodeHeaderData(data, 4, data.length);
                    System.out.println("Receiver: Printing routing data.");
                    LunarRover.rip.PrintRIPInfo();
                    // create another thread for processing
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        RIPReceiver server = new RIPReceiver();

    }
}
