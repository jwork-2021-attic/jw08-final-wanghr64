package java2021;

import java.io.ByteArrayOutputStream;

public class MyUtils {
    private MyUtils() {
    }

    public static void addInt2ByteArrayOS(ByteArrayOutputStream baos, int value) {
        baos.write(value >> 24);
        baos.write(value >> 16);
        baos.write(value >> 8);
        baos.write(value);
    }

    public static byte[] int2byteArraryBigEnd(int value) {
        byte[] res = new byte[4];
        res[0] = (byte) (value >> 24);
        res[1] = (byte) (value >> 16);
        res[2] = (byte) (value >> 8);
        res[3] = (byte) (value);
        return res;
    }
}
