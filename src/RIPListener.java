import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class RIPListener implements Runnable {

    DatagramSocket listenerSocket;
    DatagramPacket listenerPacket;
    final int PORT = 6520;
    final int BUFFER_SIZE = 1024;

    public RIPListener() {

        try {
            listenerSocket = new DatagramSocket(PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("RIP listener started");

        while (true) {

            try {
                byte[] bufferData = new byte[BUFFER_SIZE];
                listenerPacket = new DatagramPacket(bufferData, bufferData.length);
                listenerSocket.receive(listenerPacket);

                byte[] data = listenerPacket.getData();

                // data received. Now read serialize routing table

                // create another thread for processing
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        RIPListener server = new RIPListener();

    }
}
