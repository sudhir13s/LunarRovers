import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LRP {
    byte FLAGS=(byte)0;
    int SEQ=0;
    int ACK=0;
    InetAddress SOURCE_IP;
    InetAddress DESTINATION_IP;
    byte[] data;
    final static int HEADER_SIZE = 17;

    LRP() {
    }

    byte[] LRPEncodeData(int flagbit, int SEQ, int ACK, InetAddress SOURCE_IP, InetAddress DESTINATION_IP, byte[] data) {
//        int LENGTH = 9;
        byte FLAGS = 0;
        //bytes -> 1-FLAGS + 4-SEQ + 4-ACK + data
        byte[] lrpPacket = new byte[HEADER_SIZE + data.length];
        ByteBuffer byteBuffer = ByteBuffer.wrap(lrpPacket);

        FLAGS |= 1 << flagbit;

        byteBuffer.put(FLAGS);
        byteBuffer.putInt(SEQ);
        byteBuffer.putInt(ACK);
        byteBuffer.put(SOURCE_IP.getAddress());
        byteBuffer.put(DESTINATION_IP.getAddress());
        byteBuffer.put(data);
        return lrpPacket;
    }

    LRP LRPDecodeData(byte[] data) throws UnknownHostException {
        //bytes -> 1-FLAGS + 4-SEQ + 4-ACK + data
        ByteBuffer byteBuffer = ByteBuffer.wrap(data, 0, data.length);
        this.FLAGS = byteBuffer.get();
        this.SEQ = byteBuffer.getInt();
        this.ACK = byteBuffer.getInt();
        byte[] bytes_ip = new byte[4];
        byteBuffer.get(bytes_ip);
        this.SOURCE_IP = InetAddress.getByAddress(bytes_ip);
        byteBuffer.get(bytes_ip);
        this.DESTINATION_IP = InetAddress.getByAddress(bytes_ip);
        byte[] bytes_data = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes_data);
        this.data = bytes_data;
        return this;
    }

    public static void main(String[] args) {
        LRP lrp = new LRP();
        byte[] bt = {4, 0, 0, 0, 101, 0, 0, 0, -56, 116, 101, 115, 116, 32, 102, 105, 108, 101, 46, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] bytes = new byte[0];
        try {
            bytes = lrp.LRPEncodeData(1, 100, 0, InetAddress.getLocalHost(), InetAddress.getLocalHost(), new byte[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(bytes));

        try {
            lrp = lrp.LRPDecodeData(bt);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        System.out.println(lrp.FLAGS);
        System.out.println(lrp.SEQ);
        System.out.println(lrp.ACK);
        System.out.println(Arrays.toString(lrp.data));
    }
}
