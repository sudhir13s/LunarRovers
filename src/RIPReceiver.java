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

            while (true) {
                try {
                    byte[] bufferData = new byte[BUFFER_SIZE];
                    receiverPacket = new DatagramPacket(bufferData, bufferData.length);
                    socket.receive(receiverPacket);

                    byte[] data = receiverPacket.getData();

                    // data received. Now read serialize routing table
                    LunarRover.rip.RIPDecodeHeaderData(data, 4, data.length);
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
