import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LunarRover {
    final static String RIP_MULTICAST_ADDRESS = "224.0.0.9";
    final static int RIP_MULTICAST_PORT = 5520;
    private final static int RIP_PORT = 5521;
    private static DatagramSocket RIP_SOCKET;
    static Map<InetAddress, InetAddress> NEIGHBORS_ENTRIES = new ConcurrentHashMap<>();

    private final static int LRP_PORT = 45654;
    private static DatagramSocket LRP_SOCKET;
    static InetAddress source_ip;


    public LunarRover() {
    }

    /***
     * Main function of Lunar Rover.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            int nodeNum = 0;
            String destination_ip = "localhost";
            String fileName = "LunarRoverProtocolFile";

            // Get the node number (first arg)
            nodeNum = Integer.parseInt(args[0]);
            if (args.length == 3) {
                destination_ip = args[1];
                fileName = args[2];
            }

            System.out.println("I'm node " + nodeNum);
            try {
                String localhost = InetAddress.getLocalHost().getHostAddress();
//                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
//                InetAddress subnetMask = networkInterface.getInterfaceAddresses().get(0);

                System.out.println("Broadcasting from: " + localhost);
                InetAddress hostAddress = InetAddress.getByName(localhost);

                // create a child thread to update node entries in routing table.
                // implemented Runnable, but will not create thread. normal run() call.
                HostUpdate hostUpdate = new HostUpdate(args[0], hostAddress, hostAddress);
                hostUpdate.run();

                // Starting Multicast Receiver
                System.out.println("Starting Multicast Receiver...");
                Thread multicastReceiver = new Thread(new RIPMulticastReceiver(RIP_MULTICAST_ADDRESS, RIP_MULTICAST_PORT), "RIP Multicast Receiver");
                multicastReceiver.start();

                // normal ripReceiver.
                System.out.println("Starting RIP Receiver...");
                Thread ripReceiver = new Thread(new RIPReceiver(RIP_PORT, RIP_SOCKET), "RIP Receiver");
                ripReceiver.start();

                // Starting Sender
                System.out.println("Starting RIP Sender...");
                Thread sender = new Thread(new RIPSender(RIP_PORT, RIP_SOCKET), "RIP Sender");
                sender.start();

                // Garbage Collector
                System.out.println("Starting Garbage Collector...");
                Thread garbageCollector = new Thread(new GarbageCollector(), "Garbage Collector");
                garbageCollector.start();

                // let RIP settle down.
                System.out.println("Waiting for 30 seconds to settle down the routes.");
                Thread.sleep(30000);
                // now open socket for LRP
                LRP_SOCKET = new DatagramSocket(LRP_PORT);
                if (args.length == 3) {
                    // Starting lrpSender
                    System.out.println("Starting LRP Sender...");
                    System.out.println("Sending " + fileName + " file from: " + source_ip + " to " + destination_ip);
                    LRPSender lrpSender = new LRPSender(source_ip, InetAddress.getByName(destination_ip), LRP_PORT, LRP_SOCKET, fileName);
                    lrpSender.run();
                } else {
                    // Starting lrpReceiver.
                    System.out.println("Starting LRP Receiver...");
                    Thread lrpReceiver = new Thread(new LRPReceiver(source_ip, LRP_PORT, LRP_SOCKET), "LRP Receiver");
                    lrpReceiver.start();
                }

                // now loop
                while (true) {
                    Thread.sleep(5000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else
            System.out.println("No input args! Must specify Node Number!");
    }
}
