import java.net.InetAddress;
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

//    public RIP RIPDecodeHeaderData(byte[] data, int offset, int length) {
//        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, length);
//        this.destinationAddress = byteBuffer.getInt();
//        this.subnetMask = byteBuffer.getInt();
//        this.nextHopAddress = byteBuffer.getInt();
//        this.hopCount = byteBuffer.getInt();
//        return this;
    }

