package main.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.hlkgj.app.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import main.ExternalCall;
import okhttp3.Call;
import okhttp3.OkHttpClient;

public class UploadActivity extends Activity {
    public static final int REQUEST_PICK_PICTURE = 100;
    public static final int RESULT_SUCCESS = 1;
    public static final int RESULT_FAILED = -1;
    public static final int RESULT_CANCEL = 0;
    public static final float SCALE_RATIO = 0.8f;
    private static final String TAG = "UploadActivity";
    private File photo;
    private String uploadUrl;
    private ProgressBar progress;
    private String text;
    private String type;
    private boolean needUpload;
    private int cmdid;

    public static Bitmap scaleDown(Bitmap original, int maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                maxImageSize / original.getWidth(),
                maxImageSize / original.getHeight());
        int width = Math.round(ratio * original.getWidth());
        int height = Math.round(ratio * original.getHeight());

        return Bitmap.createScaledBitmap(original, width, height, filter);
    }

    public static Bitmap scaleDown(Bitmap original, float ratio,
                                   boolean filter) {
        int width = Math.round(ratio * original.getWidth());
        int height = Math.round(ratio * original.getHeight());

        return Bitmap.createScaledBitmap(original, width, height, filter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress);
        progress = (ProgressBar) findViewById(R.id.progressBar);
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
        }

        Intent intent = getIntent();
        if (intent != null) {
            uploadUrl = intent.getStringExtra("url");
            cmdid = intent.getIntExtra("cmdid", -1);
            if (TextUtils.isEmpty(uploadUrl)) {
                needUpload = true;
            }
        } else {
            handleResult(RESULT_FAILED, "no intent data");
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        pick();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        if (resultCode == Activity.RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult: canceled");
            handleResult(RESULT_CANCEL, "canceled");
            return;
        }
        switch (requestCode) {
            case REQUEST_PICK_PICTURE:
                saveToFile(data.getData());
                break;
            default:
                break;
        }
    }

    private void handleResult(int result, String message) {
        JSONObject resultJson;
        try {
            resultJson = new JSONObject();
            resultJson.put("res", result);
            if (result == RESULT_SUCCESS) {
                if (needUpload) {
                    resultJson.put("response", message);
                } else {
                    resultJson.put("img", getFileData());
                }

            } else {
                resultJson.put("error", message);
            }

        } catch (JSONException | IOException e) {
            resultJson = getResult(RESULT_FAILED);
            e.printStackTrace();
        }
        Log.i(TAG, "saveToFile currentCallback: " + resultJson.toString());
        ExternalCall.sendMessageToGame(cmdid, resultJson.toString());
        finish();
    }

    private String getFileData() throws IOException {
        if (photo != null && photo.exists()) {
            return Base64Util.fileToBase64(photo.getAbsolutePath());
        }
        return "";
    }

    private JSONObject getResult(int result) {
        JSONObject jsonObject;
        jsonObject = new JSONObject();
        try {
            jsonObject.put("res", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    private void saveToFile(Uri data) {
        photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "temp.jpg");
        if (photo.exists()) {
            photo.delete();
        }
        try {
            boolean exist = photo.createNewFile();
            if (!exist) {
                Log.e(TAG, "saveToFile: can't create file");
                handleResult(RESULT_FAILED, "can't create file");
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "saveToFile Uri: " + data.getPath());
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data);
            if (bitmap != null) {
                OutputStream os = new BufferedOutputStream(new FileOutputStream(photo));
                bitmap = scaleDown(bitmap, SCALE_RATIO, true);
                Log.i(TAG, "saveToFile: scale bitmap " + bitmap.getWidth() + ", " + bitmap.getHeight());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
                os.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "saveToFile: write temp file failed");
            handleResult(RESULT_FAILED, "can't write file");
            e.printStackTrace();
        }

        Log.i(TAG, "saveToFile: path " + photo.getPath());
//      上传由游戏端处理
        if (needUpload) {
            upload();
        } else {
            handleResult(RESULT_SUCCESS, "");
        }
    }

    private void upload() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor(TAG))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        OkHttpUtils.post()
                .url(uploadUrl)
                .addFile("file", photo.getName(), photo)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        handleResult(RESULT_FAILED, e.getMessage());
                        progress.setVisibility(View.INVISIBLE);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        handleResult(RESULT_SUCCESS, response);
                        progress.setVisibility(View.INVISIBLE);
                    }
                });
    }


    private void pick() {
        Log.d(TAG, "Pick from gallery.");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_PICTURE);
    }

    public String getPath(final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
