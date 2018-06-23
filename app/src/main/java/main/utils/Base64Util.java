package main.utils;

import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File and Base64 string converter.
 */
public class Base64Util {
    private static final String TAG = "Based64";

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            Log.e(TAG, "loadFile: file is too large");
        }
        byte[] bytes = new byte[(int) length];
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        is.close();
        return bytes;
    }

    public static String fileToBase64(String path)
            throws IOException {
        File file = new File(path);
        byte[] bytes = loadFile(file);
        //注意这里的flag，表示字符串的格式（是否换行等）
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static File base64ToFile(String base64, File file)
            throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(Base64.decode(base64, Base64.DEFAULT));
        fos.close();
        return file;
    }

}
