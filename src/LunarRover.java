import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;

public class LunarRover {

    final static int PORT = 5520;
    final static String MULTICAST_ADDRESS = "230.231.232.233";
    static HashMap<InetAddress, InetAddress> MAPPING = new HashMap<>();
    static Integer SLEEP_TIME = 5000;

    public LunarRover() {
    }

    /***
     * Main function of Lunar Rover.
     * @param args
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            // Get the node number (first arg)
            int nodeNum = Integer.parseInt(args[0]);
            System.out.println("I'm node " + nodeNum);
            try {
                String localhost = InetAddress.getLocalHost().getHostAddress();
//                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localhost);
//                InetAddress subnetMask = networkInterface.getInterfaceAddresses().get(0);

                System.out.println("Broadcasting from: " + localhost);
                InetAddress hostAddress = InetAddress.getByName(localhost);

                // create a child thread to update node entries in routing table.
                // implemented Runnable, but will not create thread. normal run() call.
                HostUpdate hostUpdate = new HostUpdate(args, hostAddress, hostAddress);
//                Thread hostUpdateThread = new Thread(hostUpdate, "Host Updater");
//                hostUpdateThread.start();
                hostUpdate.run();

                // Starting Multicast Receiver
                System.out.println("Starting Multicast Receiver...");
                Thread receiver = new Thread(new RIPReceiver(MULTICAST_ADDRESS, PORT), "RIP Receiver");
                receiver.start();

                // Starting Multicast Sender
                System.out.println("Starting Multicast Sender...");
                Thread sender = new Thread(new RIPSender(MULTICAST_ADDRESS, PORT, nodeNum), "RIP Sender");
                sender.start();

                // Garbage Collector
                System.out.println("Starting Garbage Collector...");
                Thread garbageCollector = new Thread(new GarbageCollector(), "Garbage Collector");
                garbageCollector.start();

                while (true) {
                    Thread.sleep(SLEEP_TIME);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else
            System.out.println("No input args! Must specify Node Number!");
    }

    /***
     * Processing packets received from application.
     * Client sending data on UDP port 6521
     * Server listening on UDP port 6521
     */
    // create threads to process application data. do following-
    // **Server
    // receive data on UCP port 6521
    // create new thread to process it. Now keep listening.
    // Processor -
    // read headers details.
    // fetch destination.
    // do lookup in Forwarding table/ (Dest, Next hop)
    // not found - send packets to default.
    // found -
    // change header, decrement TTL value in header.
    // forward the packets to Next Hop (matching Next hop) on UCP port 6521.
    // Client**

    /***
     * RIPv2 - sharing the routing information between connected networks.
     * Make sure, network available, otherwise find another path.
     * Client sending data on UDP port 6520
     * Server listening on UDP port 6520
     */

    // create threads to process routing information received from connected neighbors.
    // **Server

//    RIPReceiver ripReceiver = new RIPReceiver(MULTICAST_ADDRESS, PORT);
//    Thread RIPListenerThread = new Thread(ripReceiver, "RIP Receiver");
//        RIPListenerThread.start();
    // receive data on UDP port 6520
    // create a new thread to process it. Now keep listening.
    // processor -
    // read the routing table received.
    // fetch our own routing table.
    // check for update in new data. Cross check against all entries.
    // If no update - do nothing.
    // If update - Update the routing table.

    // Be careful in updating the routing table.

    // create a new thread to send update to all connected neighbors.
    // UpdatesToNeighbors
    // fetch all neighbors, start sending updated routing table.

    // Be careful in sending routing table to neighbors.
    // Don't advertise the dependable entries to dependable router.

    // Client**
//    RIPSender ripSender = new RIPSender(MULTICAST_ADDRESS, PORT, 1);
//    Thread ripSenderThread = new Thread(ripSender, "RIP Sender");
//        ripSenderThread.start();
}
