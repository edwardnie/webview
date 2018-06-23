package main.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Compress and decompress file.
 */
public class FileCompresser {
    private static final String TAG = "Zlib";

    public static File compressFile(File raw, File dest)
            throws IOException {
        byte[] rawBytes = fileToBytes(raw);
        byte[] compressed = compress(rawBytes);
        Log.i(TAG, "rawBytes: " + Arrays.toString(rawBytes));
        Log.i(TAG, "compress: " + Arrays.toString(compressed));
        bytesToFile(compressed, dest);
        return dest;
    }

    public static File decompressFile(File compressed, File raw) {
        byte[] compressedBytes = fileToBytes(compressed);
        byte[] decompressed = decompress(compressedBytes);
        bytesToFile(decompressed, raw);
        Log.i(TAG, "compressed: " + Arrays.toString(compressedBytes));
        Log.i(TAG, "decompressed: " + Arrays.toString(decompressed));
        return raw;
    }


    public static byte[] compress(byte[] data) {
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    /**
     * 压缩
     *
     * @param data 待压缩数据
     * @param os   输出流
     */
    public static void compress(byte[] data, OutputStream os) {
        DeflaterOutputStream dos = new DeflaterOutputStream(os);

        try {
            dos.write(data, 0, data.length);
            dos.finish();
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    /**
     * 解压缩
     *
     * @param is 输入流
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(InputStream is) {
        InflaterInputStream iis = new InflaterInputStream(is);
        ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
        try {
            int i = 1024;
            byte[] buf = new byte[i];

            while ((i = iis.read(buf, 0, i)) > 0) {
                o.write(buf, 0, i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return o.toByteArray();
    }

    private static byte[] fileToBytes(File file) {
        FileInputStream fileInputStream;
        byte[] bytes = new byte[(int) file.length()];
        try {
            //convert file into array of bytes
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(bytes);
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static File bytesToFile(byte[] bytes, File file) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}