package main.utils;

import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import main.ExternalCall;

/**
 * Created by yons on 16/8/12.
 */
public class RecordHelper {

    public static final String DIR_TEMP = "TEMP";
    private static final String TAG = "RecordHelper";
    public static long start;
    public static long stop;
    private static MediaRecorder recorder = new MediaRecorder();
    private static String filePath;
    private static File recordFile;

    private static File prepareFile(String type) {
        //type can be "received", "compressed", "record"
        File dir = new File(ExternalCall.context.getCacheDir(), type);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            Log.i(TAG, "create Record directory: " + String.valueOf(success));
        }
        DateFormat format = new SimpleDateFormat("MM-dd_HHmmss", Locale.getDefault());
        String time = format.format(new Date());
        return new File(dir, "record_" + time + ".m4a");
    }

    public static File createFile(String name) {
        File recordFile = prepareFile(name);
        if (!recordFile.exists()) {
            try {
                boolean success = recordFile.createNewFile();
                Log.i(TAG, "createFile: " + recordFile.getName() + " " + String.valueOf(success));
            } catch (IOException e) {
                Log.e(TAG, "createFile: failed " + recordFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return recordFile;
    }

    public static void clearCache() {
        deleteDirectory(DIR_TEMP);
    }

    private static void deleteDirectory(String name) {
        if (ExternalCall.context == null) {
            return;
        }
        try {
            File dir = new File(ExternalCall.context.getCacheDir(), name);
            for (File file :
                    dir.listFiles()) {
                boolean s = file.delete();
                if (!s) {
                    Log.i(TAG, "clearCache: delete failed");
                }
            }
            dir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void record() {
        start = System.currentTimeMillis();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(16000);
        recorder.setAudioChannels(1);
        recordFile = createFile("Record");
        recorder.setOutputFile(recordFile.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getRecordFile() {
        return recordFile;
    }

    public static void stop() {
        stop = System.currentTimeMillis();
        if (stop - start < 1000) {
            recordFile.delete();
            recorder.reset();
            return;
        }

        if (recorder != null) {
            SystemClock.sleep(1000);
            recorder.stop();
            recorder.reset();
        }
    }
}
