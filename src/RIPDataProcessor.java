import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class RIPDataProcessor implements Runnable {

    private byte[] receivedBytes;
    private int offset;

    RIPDataProcessor(byte[] bytes) {
        this.receivedBytes = bytes;
        this.offset = 0;
    }

    private void ProcessInputRIPData() {
        //received raw data.
        boolean isRouteChanged = false;
        RoutingTable rt = new RoutingTable();
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.receivedBytes);
        byteBuffer.order(ByteOrder.nativeOrder());

        synchronized (RoutingTable.routeEntriesLock) {
            try {
                RIP rip = new RIP();
                rip = rip.RIPDecodeData(this.receivedBytes, this.offset, this.receivedBytes.length);

                Iterator<RIPEntry> iterator = rip.ripEntries.iterator();
                while (iterator.hasNext()) {
                    RIPEntry entry = iterator.next();
                    NextHopInfoTable nextHopInfo = new NextHopInfoTable(entry.getSubnetMask(), entry.getNextHopAddress(),
                            0, entry.getHopCount());

                    System.out.println("RIPDataProcessor: calling updateRoutingTable");
                    if (rt.updateRoutingTable(entry.getDestinationAddress(), nextHopInfo)) {
                        isRouteChanged = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // if route table updated.
            if (isRouteChanged) {
                // update special flag to trigger update.
                RoutingTable.isTriggerUpdate = true;
                System.out.println("RIPDataProcessor: Routing table updated.");
                // print routing table.
                rt.PrintRoutingTable();
            }
        }
        // if RoutingTable.isTriggerUpdate flag is set, send trigger update.
        if (RoutingTable.isTriggerUpdate) {
            Thread ripTriggerUpdate = new Thread(new RIPTriggerUpdate(LunarRover.MULTICAST_ADDRESS, LunarRover.PORT), "RIP Trigger Update");
            ripTriggerUpdate.start();
        }
    }

    private void ProcessInputRIPResponse() {
//        RIP rip = ProcessInputRIPHeader();
//        ProcessInputRIPData();
    }

    private void ProcessInputData() {
        switch (this.receivedBytes[0]) {
            case 1:
                // ProcessRIPRequest(); // implement later.
            case 2:
                ProcessInputRIPData();
            default:
                System.err.println("Unknown RIP command.: " + this.receivedBytes[0]);
        }
    }

    @Override
    public void run() {
        try {
            ProcessInputData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        byte[] bufferData = RIPReceiver.hexStringToByteArray("0202000000020001c0a83803ffffff00c0a838030000000100020001c0a83803ffffff00c0a8380300000001");
        RIPDataProcessor ripDataProcessor = new RIPDataProcessor(bufferData);
        ripDataProcessor.run();
    }
}
