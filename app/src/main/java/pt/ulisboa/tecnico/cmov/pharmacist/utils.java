package pt.ulisboa.tecnico.cmov.pharmacist;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

public class utils {
    // convert bitmap to byteArray
    public static String bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Arrays.toString(stream.toByteArray());

    }


    public static Bitmap convertCompressedByteArrayToBitmap(String src) {
        String[] byteValues = src.substring(1, src.length() - 1).split(",");
        byte[] bytes = new byte[byteValues.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            bytes[i] = Byte.parseByte(byteValues[i].trim());
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }


}
