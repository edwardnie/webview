package main.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hlkgj.app.R;
import com.hlkgj.app.BuildConfig;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

import layaair.game.browser.ConchCanvas;
import main.ExternalCall;
import main.MainActivity;
import main.MainApp;

import static android.content.Context.LOCATION_SERVICE;
import static main.ExternalCall.context;
import static main.MainApp.appContext;

/**
 * Created by yons on 16/8/12.
 */
public class Utils {
    private static final String TAG = "Utils";

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getAppName() {
        return context.getString(R.string.app_name);
    }

    public static String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    public static String getUniqueId() {
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void registerBattery(int cmdid) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        BroadcastReceiver receiver = new BatteryReceiver(cmdid);
        context.registerReceiver(receiver, filter);
    }

    public static void addShortCut() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainApp.appContext);
        boolean firstEnter = sp.getBoolean("first", true);
        if (firstEnter) {
            sp.edit().putBoolean("first", false).apply();
            Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
            intent.putExtra("duplicate", false);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appContext.getString(R.string.app_name));
            Parcelable icon = Intent.ShortcutIconResource.fromContext(appContext, R.mipmap.ic_launcher);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(appContext, MainActivity.class));
            context.sendBroadcast(intent);
            Log.i(TAG, "addShortCut: ");
        }
    }

    public static void installApk(String url) {
        new DownloadHelper(context, url).downWithDownloadManager();
//        String fileName = url.substring(url.lastIndexOf('/') + 1);
//        OkHttpUtils.get().url(url).build().execute(new FileCallBack(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
//                fileName) {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                Toast.makeText(context, "下載失敗。", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onResponse(File response, int id) {
//                Utils.install(response);
//            }
//        });
    }

    public static boolean isWechatInstalled() {
        final PackageManager packageManager = appContext.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static double playAudio(File audioFile, float vol) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        double seconds = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(audioFile);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.setVolume(vol, vol);
            mediaPlayer.prepare();
            mediaPlayer.start();
            int duration = mediaPlayer.getDuration();
            if (duration <= 0) {
                return 0;
            }
            seconds = duration / 1000.0;
            Log.i(TAG, "playAudio: OK");
            Log.i(TAG, "getDuration: duration-" + seconds);
        } catch (IOException e) {
            Log.i(TAG, "playAudio: failed");
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }

        }
        return seconds;
    }

    public static void takeScreenshot(UpdateData updateData) {
        ConchCanvas.Renderer.takeScreenshot = true;
        ConchCanvas.Renderer.updateData = updateData;
    }

    public static Bitmap screenShot(GL10 gl, int width, int height) {
        Log.i(TAG, "screenShot: width " + width + " height " + height);
        int screenshotSize = width * height;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize - width, -width, 0, 0, width, height);

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        //Making created bitmap (from OpenGL points) compatible with Android bitmap
        for (int i = 0; i < screenshotSize; ++i) {
            short v = sBuffer[i];
            sBuffer[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        return bitmap;
    }

    public static Bitmap getScreenBitmap() {
        return ConchCanvas.Renderer.screenBitmap;
    }

    public static void fade(final View img) {
        Animation fadeOut = new AlphaAnimation(1, 0);
//        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(800);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                img.setVisibility(View.GONE);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        img.startAnimation(fadeOut);
    }

    public static void install(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void vibrate(long milliseconds) {
        Vibrator v = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (milliseconds == 0) {
            milliseconds = 400;
        }
        v.vibrate(milliseconds);
    }

    public static void copyToClipboard(final String text) {
        ExternalCall.context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ClipboardManager clipboard = (ClipboardManager) ExternalCall.context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", text);
                clipboard.setPrimaryClip(clip);
//                Toast.makeText(MainApp.appContext, text + " 已复制，请长按粘贴~", Toast.LENGTH_LONG).show();
            }
        });

    }

    public static String getTextFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) MainApp.appContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData data = clipboard.getPrimaryClip();
        String text = "";
        if (data != null && data.getItemAt(0) != null) {
            text = data.getItemAt(0).getText().toString();
        }

        Log.d(TAG, "getTextFromClipboard: " + text);
        return text;
    }

    public static boolean isInstalled(String packageName) {
        try {
            MainApp.appContext.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Nullable
    public static JSONObject getLocation(final int cmdid) {
        new TedPermission(MainActivity.activity)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        JSONObject jsonObject = retrieveLocationData();
                        if (jsonObject == null) {
                            try {
                                jsonObject = new JSONObject();
                                jsonObject.put("error", "can't get location");
                                Log.e(TAG, "can't get location: getlocation");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        ExternalCall.sendMessageToGame(cmdid, jsonObject);
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> arrayList) {
                        ExternalCall.sendMessageToGame(cmdid, "{\"error\":\"permission denied\"}");
                    }
                })
                .setDeniedMessage(context.getString(R.string.permission_hint))
                .setPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .check();

        return retrieveLocationData();
    }

    private static JSONObject retrieveLocationData() {
        Location location = getMyLocation();
        if (location == null) {
            return null;
        }
        JSONObject locationData = new JSONObject();
        double lon = location.getLongitude();
        double lat = location.getLatitude();
        double altitude = location.getAltitude();
        float bearing = location.getBearing();
        float speed = location.getSpeed();
        String gps = lon + "_" + lat + "_" + altitude + "_" + bearing + "_" + speed;
        Log.d(TAG, "getLocation: " + gps);
        try {
            locationData.put("gps", gps);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return locationData;
    }

    public static Location getMyLocation() {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location location = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (location == null || l.getAccuracy() < location.getAccuracy()) {
                // Found best last known location: %s", l);
                location = l;
            }
        }
        return location;
    }

    public static void restartApp() {
        Intent restart = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(restart);
    }
}
