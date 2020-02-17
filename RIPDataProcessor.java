import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class RIPDataProcessor implements Runnable {
    private byte[] receivedBytes;
    private int offset;
    private InetAddress addressReceived;

    RIPDataProcessor(byte[] bytes, InetAddress addressReceived) {
        this.receivedBytes = bytes;
        this.offset = 0;
        this.addressReceived = addressReceived;
    }

    private synchronized void ProcessInputRIPData() {
        boolean isRouteChanged = false;
        RoutingTable rt = new RoutingTable();
        ByteBuffer byteBuffer = ByteBuffer.wrap(this.receivedBytes);
        byteBuffer.order(ByteOrder.nativeOrder());
        try {
            RIP rip = new RIP();
            rip = rip.RIPDecodeData(this.receivedBytes, this.offset, this.receivedBytes.length);

            Iterator<RIPEntry> iterator = rip.ripEntries.iterator();
            while (iterator.hasNext()) {
                RIPEntry entry = iterator.next();
                NextHopInfoTable nextHopInfo = new NextHopInfoTable(entry.getSubnetMask(), this.addressReceived,
                        0, entry.getHopCount());

                // if receiving data from same ip. skip it.
                if (!(nextHopInfo.nextHopAddress.equals(InetAddress.getByName(InetAddress.getLocalHost().getHostAddress())) ||
                        nextHopInfo.nextHopAddress.equals(InetAddress.getByName("0.0.0.0")))) {
                    if (rt.updateRoutingTable(entry.getDestinationAddress(), nextHopInfo)) {
                        isRouteChanged = true;
                    }
                }
            }
            // if route table updated. if RoutingTable.isTriggerUpdate flag is set, send trigger update.
            if (isRouteChanged) {
//                rt.PrintRoutingTable();
                RIPTriggerUpdate.isTriggerUpdate = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ProcessInputData() {
        switch (this.receivedBytes[0]) {
            case 1:
                // ProcessRIPRequest(); // implement later.
                // break;
            case 2:
                ProcessInputRIPData();
                break;
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
//        byte[] bufferData = RIPMulticastReceiver.hexStringToByteArray("0202000000020001c0a83803ffffff00c0a838030000000100020001c0a83803ffffff00c0a8380300000001");
//        RIPDataProcessor ripDataProcessor = new RIPDataProcessor(bufferData);
//        ripDataProcessor.run();
    }
}
