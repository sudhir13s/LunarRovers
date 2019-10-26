import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class RIP {

    private byte version = 2;
    private byte command = 2; //default to 2;
    List<RIPEntry> ripEntries;

    public RIP() {
        this.version = 2;
        this.command = 2;
        this.ripEntries = new LinkedList<>();
    }

    RIP RIPDecodeData(byte[] data, int offset, int length) {
        int entrySize = 4 * 5; // 4 bytes - 5 rows.
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, offset, length);
        try {
            this.command = byteBuffer.get(1);
            this.version = byteBuffer.get(1);
            // next 2 bytes are zero.
            byteBuffer.getShort();
            // now process data.
            this.ripEntries = new LinkedList<>();
            while (byteBuffer.position() < byteBuffer.limit()) {
                RIPEntry ripEntry = new RIPEntry();
                ripEntry.RIPDecodeData(data, byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
                byteBuffer.position(byteBuffer.position() + entrySize);
                this.ripEntries.add(ripEntry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public byte[] RIPEncodeData() {
        int length = 1 + 1 + 2 + this.ripEntries.size() * (5 * 4); // total entry size.
        byte[] ripData = new byte[length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(ripData);
        byteBuffer.put(this.command);
        byteBuffer.put(this.version);
        byteBuffer.putShort((short) 0);

        for (RIPEntry ripEntry : this.ripEntries) {
            byteBuffer.put(ripEntry.RIPEncodeData());
        }
        return ripData;
    }
}

