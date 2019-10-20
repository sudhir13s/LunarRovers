import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RIPReceiver implements Runnable {

    DatagramSocket receiverSocket;
    DatagramPacket receiverPacket;
    final int PORT = 6520;
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
        System.out.println("RIP listener started");
        try {
            receiverSocket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while (true) {

            try {
                byte[] bufferData = new byte[BUFFER_SIZE];
                receiverPacket = new DatagramPacket(bufferData, bufferData.length);
                receiverSocket.receive(receiverPacket);

                byte[] data = receiverPacket.getData();

                // data received. Now read serialize routing table
                LunarRover.rip.RIPDecodeHeaderData(data, 4, data.length);
                LunarRover.rip.PrintRIPInfo();
                // create another thread for processing
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        RIPReceiver server = new RIPReceiver();

    }
}
