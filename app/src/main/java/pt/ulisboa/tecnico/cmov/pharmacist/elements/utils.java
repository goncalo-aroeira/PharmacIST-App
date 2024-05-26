package pt.ulisboa.tecnico.cmov.pharmacist.elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Random;

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

    public static String generateRandomId(int length) {
        // Define the characters to be used in the random ID
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Create a StringBuilder to hold the random ID
        StringBuilder sb = new StringBuilder(length);

        // Create a Random object
        Random random = new Random();

        // Generate random characters and append them to the StringBuilder
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        // Convert StringBuilder to String and return the random ID
        return sb.toString();
    }

}
