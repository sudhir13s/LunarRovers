import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class RIPEntry {

    private short addressFamilyIpv4 = 2;
    private short routeTag = 1;
    private InetAddress destinationAddress;
    private InetAddress subnetMask;
    private InetAddress nextHopAddress;
    private int hopCount;

    RIPEntry() {
    }

    RIPEntry(InetAddress destinationAddress, InetAddress subnetMask,
             InetAddress nextHopAddress, Integer hopCount) {
        this.destinationAddress = destinationAddress;
        this.subnetMask = subnetMask;
        this.nextHopAddress = nextHopAddress;
        this.hopCount = hopCount;
    }

    RIPEntry RIPDecodeData(byte[] data, int offset, int length) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, length);
        byte[] bytes = new byte[4];
        try {
            this.addressFamilyIpv4 = byteBuffer.getShort();
            this.routeTag = byteBuffer.getShort();
            byteBuffer.get(bytes);
            this.destinationAddress = InetAddress.getByAddress(bytes);
            byteBuffer.get(bytes);
            this.subnetMask = InetAddress.getByAddress(bytes);
            byteBuffer.get(bytes);
            this.nextHopAddress = InetAddress.getByAddress(bytes);
            this.hopCount = byteBuffer.getInt();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return this;
    }

    public byte[] RIPEncodeData() {
        byte[] ripData = new byte[20]; //20 bytes - 2*2 + 4*4
        ByteBuffer byteBuffer = ByteBuffer.wrap(ripData);
        byteBuffer.putShort(this.addressFamilyIpv4);
        byteBuffer.putShort(this.routeTag);
        byteBuffer.put(destinationAddress.getAddress());
        byteBuffer.put(subnetMask.getAddress());
        byteBuffer.put(nextHopAddress.getAddress());
        byteBuffer.putInt(hopCount);
        return ripData;
    }

    public short getAddressFamilyIpv4() {
        return this.addressFamilyIpv4;
    }

    public short getRouteTag() {
        return this.routeTag;
    }

    public InetAddress getDestinationAddress() {
        return this.destinationAddress;
    }

    public InetAddress getSubnetMask() {
        return this.subnetMask;
    }

    public InetAddress getNextHopAddress() {
        return this.nextHopAddress;
    }

    public int getHopCount() {
        return this.hopCount;
    }
}

