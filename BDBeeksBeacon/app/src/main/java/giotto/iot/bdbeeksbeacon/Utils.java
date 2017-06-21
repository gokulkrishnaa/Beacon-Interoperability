package giotto.iot.bdbeeksbeacon;

import android.util.Base64;
import android.view.View;

//import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
//import com.google.android.gms.auth.UserRecoverableAuthException;
//import com.google.android.gms.common.GooglePlayServicesUtil;

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

    public static void setEnabledViews(boolean enabled, View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View v : views) {
            v.setEnabled(enabled);
        }
    }

}
