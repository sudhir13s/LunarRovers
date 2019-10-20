import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class RIP {

    public static final byte VERSION = 2;
    //    public static final byte COMMAND_REQUEST = 1;
    public static final byte COMMAND_RESPONSE = 2;
    public static final short ADDRESS_FAMILY_IPv4 = 2;
    public static short ROUTE_TAG = 1;

    protected InetAddress destinationAddress;
    protected InetAddress subnetMask;
    protected InetAddress nextHopAddress;
    protected int hopCount;

    public RIP() {
    }

    public RIP(InetAddress destinationAddress, InetAddress subnetMask,
               InetAddress nextHopAddress, int hopCount) {
        this.destinationAddress = destinationAddress;
        this.subnetMask = subnetMask;
        this.nextHopAddress = nextHopAddress;
        this.hopCount = hopCount;
    }

    public void PrintRIPInfo() {
        System.out.println(this.destinationAddress);
        System.out.println(this.subnetMask);
        System.out.println(this.nextHopAddress);
        System.out.println(this.hopCount);
    }

    public byte[] RIPEncodeHeader() {
        byte[] ripTopHeader = new byte[4]; // 4 bytes - 1 + 1 + 2
        ByteBuffer byteBuffer = ByteBuffer.wrap(ripTopHeader);
        byteBuffer.put(COMMAND_RESPONSE);
        byteBuffer.put(VERSION);
        byteBuffer.putShort((short) 0);
        return ripTopHeader;
    }

    public byte[] RIPEncodeHeaderData(InetAddress destinationAddress, InetAddress subnetMask,
                                      InetAddress nextHopAddress, Integer hopCount) {
        byte[] ripHeader = new byte[20]; //20 bytes - 2*2 + 4*4
        ByteBuffer byteBuffer = ByteBuffer.wrap(ripHeader);
        byteBuffer.putShort(ADDRESS_FAMILY_IPv4);
        byteBuffer.putShort(ROUTE_TAG);
        byteBuffer.put(destinationAddress.getAddress());
        byteBuffer.put(subnetMask.getAddress());
        byteBuffer.put(nextHopAddress.getAddress());
        byteBuffer.putInt(hopCount);
        return ripHeader;
    }

    public RIP RIPDecodeHeaderData(byte[] data, int offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, length);

        byte[] bytes = new byte[4];
        byteBuffer.get(bytes);

        try {
            this.destinationAddress = InetAddress.getByAddress(bytes);
            this.subnetMask = InetAddress.getByAddress(bytes);
            this.nextHopAddress = InetAddress.getByAddress(bytes);
            this.hopCount = byteBuffer.getInt();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return this;
    }
}

