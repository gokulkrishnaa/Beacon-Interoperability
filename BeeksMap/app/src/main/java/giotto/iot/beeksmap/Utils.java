package giotto.iot.beeksmap;


import android.util.Base64;

import java.util.HashMap;


public class Utils {
    private Utils() {}  // static functions only

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static byte[] base64Decode(String s) {
        return Base64.decode(s, Base64.DEFAULT);
    }

    public static String base64Encode(byte[] b) {
        return Base64.encodeToString(b, Base64.DEFAULT).trim();
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) return null;

        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int c = bytes[i] & 0xFF;
            chars[i * 2] = HEX[c >>> 4];
            chars[i * 2 + 1] = HEX[c & 0x0F];
        }
        return new String(chars).toLowerCase();
    }

    public static byte[] hexToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String toHexString(int n) {
        // call toUpperCase() if that's required
        return String.format("%8s", Integer.toHexString(n)).replace(' ', '0');
    }

    public static String toHexString(float f) {
        // change the float to raw integer bits(according to the OP's requirement)
        return toHexString(Float.floatToRawIntBits(f));
    }

    private static String latlongBeacon(String mac){
        HashMap latlong = new HashMap();

        latlong.put("CA:61:94:A2:26:7D", "1500, 2010");
        latlong.put("DD:E8:59:52:47:12", "2010, 2010");
        latlong.put("DB:EF:A6:A2:A1:13", "1250, 1800");

        return String.valueOf(latlong.get(mac));
    }

}

