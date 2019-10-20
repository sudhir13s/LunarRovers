import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class LunarRover {

    static RoutingTable routingTable;
    static ArrayList<InetAddress> neighbors;
    static RIP rip;

    public LunarRover() {
        routingTable = new RoutingTable();
        neighbors = new ArrayList<>();
        rip = new RIP();
    }

    /***
     * Main function of Lunar Rover.
     * @param args
     */
    public static void main(String[] args) {

        routingTable = new RoutingTable();
//        try {
//            RoutingTable.routeEntries.put(InetAddress.getByName("127.0.0.1"),
//                    new NextHopInfoTable(InetAddress.getByName("255.255.255.0"),
//                            InetAddress.getByName("127.0.0.1"), 1));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        neighbors = new ArrayList<>();
//        try {
//            neighbors.add(InetAddress.getByName("127.0.0.1"));
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }
        rip = new RIP();

        // read routine table file and update.
        RoutingTableUpdater updater = new RoutingTableUpdater();
        Thread RTUpdaterThread = new Thread(updater, "Routing table updater");
        RTUpdaterThread.start();

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

        RIPReceiver ripReceiver = new RIPReceiver();
        Thread RIPListenerThread = new Thread(ripReceiver, "RIP Receiver");
        RIPListenerThread.start();
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
        RIPSender ripSender = new RIPSender();
        Thread ripSenderThread = new Thread(ripSender, "RIP Sender");
        ripSenderThread.start();


        /***
         * Main application to launch below daemons -
         * Create thread for processing application data.
         * Create thread for processing routing table.
         */

    }

    public static void PrintNeighbor() {
        for (int i = 0; i < neighbors.size(); i++) {
            System.out.println("Neighbor: " + neighbors.get(i));
        }
    }
}
