package main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.ValueCallback;
import android.widget.Toast;

import com.hlkgj.app.wxapi.WXEntryActivity;
import com.hlkgj.app.wxapi.WXHelper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import cn.jpush.android.api.JPushInterface;
import main.utils.AESCipher;
import main.utils.Base64Util;
import main.utils.FileCompresser;
import main.utils.NetMonitor;
import main.utils.RecordHelper;
import main.utils.SocketHelper;
import main.utils.SpUtil;
import main.utils.UdeskHelper;
import main.utils.UpdateData;
import main.utils.UploadActivity;
import main.utils.Utils;
import okhttp3.Call;
import com.alipay.sdk.app.PayTask;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.Map;
import android.text.TextUtils;

/**
 * Created by yons on 16/8/10.
 */
public class ExternalCall implements UpdateData {
    public static final String TAG = "ExternalCall";
    private static final int SDK_PAY_FLAG = 1;
    public static final int CMD_OPEN_SPLASH = 0;
    public static final int CMD_CLOSE_SPLASH = 1;
    public static final int CMD_RECORD_AUDIO = 2;
    public static final int CMD_STOP_AUDIO = 3;
    public static final int CMD_PLAY_AUDIO = 4;
    public static final int CMD_SHARE_ROOM = 5;
    public static final int CMD_SHARE_REPLAY = 6;
    public static final int CMD_SHARE_RESULT = 7;
    public static final int CMD_WECHAT_PAY = 8;
    public static final int CMD_GET_APP_INFO = 9;
    public static final int CMD_CHECK_URL = 10;
    public static final int CMD_GET_BATTERY = 11;
    public static final int CMD_SET_JPUSH_ALIAS = 12;
    public static final int CMD_OPEN_URL = 13;
    public static final int CMD_RESTART_APP = 14;
    public static final int CMD_ADD_SHORTCUT = 15;
    public static final int CMD_VIBRATE = 18;
    public static final int CMD_MONITOR_NETWORK = 19;
    public static final int CMD_SHARE_TIMELINE = 20;
    public static final int CMD_COPY_TO_CLIPBOARD = 21;
    public static final int CMD_CHECK_APPS_INSTALL = 22;
    public static final int CMD_UPLOAD_PICTURE = 23;
    public static final int CMD_GET_DEEPLINK_URI = 24;
    public static final int CMD_SOCKECT_CONNECT = 25;
    public static final int CMD_GET_LOCATION = 26;
    public static final int CMD_ENCRYPT_AES = 27;
    public static final int CMD_DECRYPT_AES = 28;
    public static final int CMD_START_RECORDING = 29;
    public static final int CMD_READ_CLIPBOARD = 30;
    public static final int CMD_SHARE_TEXT_TO_WECHAT = 31;
    public static final int CMD_CUSTOMER_SERVICE = 35;
    public static final int CMD_ALIPAY_PAY=200;

    //MSG sent to game
    public static final String MSG_RETURN_GAME = "game returned";

    public static Activity context;
    public static ExternalCall EXTERNAL_CALL;
    public static int batteryLevel;
    public static int currentCmd;
    public static int currentCmdId;
    public static int alipayCmdID;
    public static ValueCallback<JSONObject> currentCallback;
    public static SparseArray<ValueCallback<JSONObject>> callbacks = new SparseArray<>();
    private static JSONObject data;
    private boolean isTimeline;
    public static String resultInfo="";
    public static String resultStatus="";
    private ExternalCall(Activity context) {
        ExternalCall.context = context;
    }

    public static ExternalCall makeInstance(Activity activity) {
        if (EXTERNAL_CALL == null) {
            EXTERNAL_CALL = new ExternalCall(activity);
        }
        return EXTERNAL_CALL;
    }

    public static void sendMessageToGame(int cmdid, String msg) {
        ValueCallback<JSONObject> callback = callbacks.get(cmdid);
        if (callback != null) {
            JSONObject data = new JSONObject();
            try {
                data.put("cmdid", cmdid);
                data.put("data", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callback.onReceiveValue(data);
            callbacks.remove(cmdid);
        }
    }

    public static void sendMessageToGame(int cmdid, JSONObject msg) {
        ValueCallback<JSONObject> callback = callbacks.get(cmdid);
        if (callback != null) {
            callback.onReceiveValue(msg);
        }
        callbacks.remove(cmdid);
    }


    /**
     * this method will send msg instantly with current messageCallback
     *
     * @param callbackData data need sending to game
     */
    public void messageCallback(String callbackData) {
        if (currentCallback == null) {
            Log.e(TAG, "sendMessageToGame: current messageCallback is null----- " + callbackData);
            sendErrorMessage("current callback is null");
            return;
        }
        callbacks.remove(currentCmdId);
        JSONObject text = new JSONObject();
        try {
            text.put("data", callbackData);
            text.put("cmdid", currentCmdId);
            currentCallback.onReceiveValue(text);
        } catch (JSONException e) {
            sendErrorMessage(e.getMessage());
            e.printStackTrace();
        }
    }


    public void call(int cmd, final int cmdid, String body, final ValueCallback<JSONObject> callback) {
        Log.i(TAG, "call: cmd " + cmd + " body " + body);
        ExternalCall.currentCmd = cmd;
        ExternalCall.currentCmdId = cmdid;
        ExternalCall.currentCallback = callback;
        callbacks.put(cmdid, callback);

        switch (cmd) {
            case CMD_OPEN_SPLASH:
                MainActivity.showSplash();
                messageCallback("");
                break;
            case CMD_CLOSE_SPLASH:
                MainActivity.hideSplash();
                Utils.addShortCut();
                MainActivity.onLoginUi();
                messageCallback("");
                break;
            case CMD_RECORD_AUDIO:
                RecordHelper.record();
                messageCallback("");
                break;
            case CMD_STOP_AUDIO:
                RecordHelper.stop();
                try {
                    File compressed = RecordHelper.createFile(RecordHelper.DIR_TEMP);
                    FileCompresser.compressFile(RecordHelper.getRecordFile(), compressed);
                    String encoded = Base64Util.fileToBase64(compressed.getAbsolutePath());
                    messageCallback(encoded);
                } catch (IOException e) {
                    Log.e(TAG, "call: compress audio failed");
                    e.printStackTrace();
                    messageCallback("");
                }
                break;
            case CMD_PLAY_AUDIO:
                try {
                    JSONObject data = new JSONObject(body);
                    String encoded = data.getString("buf");
                    File received = Base64Util.base64ToFile(encoded, RecordHelper.createFile(RecordHelper.DIR_TEMP));
                    File raw = RecordHelper.createFile("Decompressed");
                    FileCompresser.decompressFile(received, raw);
                    Log.i(TAG, "call: received " + received.getPath());
                    float vol = BigDecimal.valueOf(data.getDouble("vol")).floatValue();
                    double du = Utils.playAudio(raw, vol);
                    messageCallback(String.valueOf(du));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    messageCallback("");
                }
                break;
            case CMD_SHARE_ROOM:
                try {
                    JSONObject data = new JSONObject(body);
                    String url = data.getString("url");
                    String title = data.getString("title");
                    String desc = data.getString("desc");
                    String picPath = data.optString("picPath");
                    Boolean timeline=false;
                    if (data.has("timeline"))
                    {
                        timeline=data.getBoolean("timeline");
                    }
                    if (!timeline)
                    {
                        WXHelper.shareLinkToWechat(url, title, desc, picPath);
                    }
                    else
                    {
                        WXHelper.shareLinkToTimeline(url,title,desc,picPath);
                    }
                    WXEntryActivity.registerCallback(cmdid);
                } catch (Exception e) {
                    Log.e(TAG, "call: share room error, missing param");
                    e.printStackTrace();
                }
                break;
            case CMD_SHARE_REPLAY:
                try {
                    JSONObject data = new JSONObject(body);
                    String url = data.getString("url");
                    String title = data.getString("title");
                    String desc = data.getString("desc");
                    String picPath = data.optString("picPath");
                    WXHelper.shareLinkToWechat(url, title, desc, picPath);
                } catch (Exception e) {
                    Log.e(TAG, "call: share room error, missing param");
                    e.printStackTrace();
                }
                messageCallback("");
                break;
            case CMD_SHARE_RESULT:
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    isTimeline = jsonObject.optBoolean("timeline");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                WXEntryActivity.registerCallback(cmdid);
                Utils.takeScreenshot(this);//截图，然后在onReceived方法中回调
                break;
            case CMD_WECHAT_PAY:
                try {
                    JSONObject jsonObject = new JSONObject(body);
                    WXHelper.StartPay(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CMD_GET_APP_INFO:
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("ver", Utils.getVersionName())
                            .put("appname", Utils.getAppName())
                            .put("bundleid", Utils.getPackageName())
                            .put("country", Utils.getCountry())
                            .put("appleLang", "")
                            .put("langareacode", "zh-CN")
                            .put("vercode", Utils.getVersionCode())
                            .put("adid", "")
                            .put("adtrack", "0")
                            .put("duid", Utils.getUniqueId())
                            .put("name", Build.DEVICE)
                            .put("systemName", Build.VERSION.CODENAME)
                            .put("systemVersion", Build.VERSION.SDK_INT)
                            .put("localizedModel", "")
                            .put("deviceModel", Build.MODEL);
//                    Log.i(TAG, "call: app info " + jsonObject.toString());
                } catch (Exception e) {
                    sendErrorMessage(e.getMessage());
                    e.printStackTrace();
                }
                messageCallback(jsonObject.toString());
                break;
            case CMD_CHECK_URL:
//nothing here
                break;
            case CMD_GET_BATTERY:
                Utils.registerBattery(cmdid);
                break;
            case CMD_SET_JPUSH_ALIAS:
                try {
                    JSONObject data = new JSONObject(body);
                    String alias = data.getString("alias");
                    Log.i(TAG, "jpush: alias " + alias);
                    JSONArray array = data.getJSONArray("tags");
                    Set<String> tags = new HashSet<>();
                    for (int i = 0; i < array.length(); i++) {
                        tags.add(array.getString(i));
                    }
                    JPushInterface.setAliasAndTags(context, alias, tags, null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                messageCallback("");
                break;
            case CMD_OPEN_URL:
                try {
                    JSONObject data = new JSONObject(body);
                    String url = data.getString("url");
                    if (url.endsWith(".apk")) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context.getApplicationContext(), "开始下载安裝文件...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Utils.installApk(url);
                    } else {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                messageCallback("");
                break;
            case CMD_RESTART_APP:
                Log.i(TAG, "call: restart app");
                Utils.restartApp();
                break;
            case CMD_ADD_SHORTCUT:
                Utils.addShortCut();
                messageCallback("");
                break;
            case CMD_VIBRATE:
                long time = 0;
                try {
                    JSONObject data = new JSONObject(body);
                    time = data.optLong("du");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Utils.vibrate(time);
                messageCallback("");
                break;
            case CMD_MONITOR_NETWORK:
                NetMonitor.getInstance(cmdid).monitor();
                break;
            case CMD_SHARE_TIMELINE:
                try {
                    JSONObject data = new JSONObject(body);
                    String url = data.getString("url");
                    String title = data.getString("title");
                    String desc = data.getString("desc");
                    String picPath = data.optString("picPath");
                    WXHelper.shareLinkToTimeline(url, title, desc, picPath);
                    WXEntryActivity.registerCallback(cmdid);
                } catch (Exception e) {
                    Log.e(TAG, "call: share room error, missing param");
                    e.printStackTrace();
                }
                break;
            case CMD_COPY_TO_CLIPBOARD:
                String title = "";
                try {
                    JSONObject data = new JSONObject(body);
                    title = data.optString("title");
                    String url = data.getString("url");
                    Utils.copyToClipboard(url);
                } catch (Exception e) {
                    Utils.copyToClipboard(title);
                    e.printStackTrace();
                }
                messageCallback("");
                break;
            case CMD_CHECK_APPS_INSTALL:
                Log.d(TAG, "call:CMD_CHECK_APPS_INSTALL " + body);
                JSONArray array;
                JSONObject data;
                try {
                    data = new JSONObject();
                    array = new JSONArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject json = array.getJSONObject(i);
                        String packageName = json.getString("appurl");
                        if (Utils.isInstalled(packageName)) {
                            json.put("isInstalled", 1);
                            array.put(i, json);
                        }
                    }
                    messageCallback(array.toString());
                    Log.d(TAG, "call: " + array.toString());
                    callback.onReceiveValue(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CMD_UPLOAD_PICTURE:
                callbacks.put(cmdid, callback);
                try {
                    JSONObject json = new JSONObject(body);
                    Intent upload = new Intent(MainApp.appContext, UploadActivity.class);
                    upload.putExtra("url", json.getString("uploadurl"));
                    upload.putExtra("cmdid", cmdid);
                    context.startActivity(upload);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case CMD_SOCKECT_CONNECT:
                new SocketHelper(body, cmdid).newConnect();
                break;
            case CMD_GET_LOCATION:
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject locationData = Utils.getLocation(cmdid);
                        if (locationData != null) {
                            callback.onReceiveValue(locationData);
                        }
                    }
                });
                break;
            case CMD_ENCRYPT_AES:
                try {
                    JSONObject aData = new JSONObject(body);
                    String content = aData.getString("content");
                    String key = aData.optString("key");
                    if (key.length() < 16) {
                        key = "tianaweishayao16";
                    }
                    String callbackData = AESCipher.encryptAES(content, key);
                    messageCallback(callbackData);

                } catch (JSONException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                    e.printStackTrace();
                    messageCallback("");
                }
                break;
            case CMD_DECRYPT_AES:
                try {
                    JSONObject aData = new JSONObject(body);
                    String content = aData.getString("content");
                    String key = aData.optString("key");
                    if (key.length() < 16) {
                        key = "tianaweishayao16";
                    }
                    String callbackData = AESCipher.decryptAES(content, key);
                    messageCallback(callbackData);

                } catch (JSONException | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | NoSuchPaddingException e) {
                    e.printStackTrace();
                    messageCallback("");
                }
                break;
            case CMD_START_RECORDING:
                SpUtil.save("cmdid", cmdid);
                break;
            case CMD_READ_CLIPBOARD:
                final int clipcmdid = cmdid;
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sendMessageToGame(clipcmdid, Utils.getTextFromClipboard());
                    }
                });
                break;
            case CMD_GET_DEEPLINK_URI:
                Log.d(TAG, "call: read deeplink" + SpUtil.get("uri"));
                messageCallback(SpUtil.get("uri"));
                SpUtil.remove("uri");
                break;
            case CMD_SHARE_TEXT_TO_WECHAT:
                try {
                    JSONObject textObject = new JSONObject(body);
                    String type = textObject.optString("type");
                    if (type.equals("alipay")) {
                        shareAliPay("");
                    } else {
                        String text = textObject.getString("text");
                        Utils.copyToClipboard(text);
                        WXHelper.goWechat();
//                      WXHelper.shareToWechat(text, false); 直接分享文本在微信无法复制
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CMD_CUSTOMER_SERVICE:
                try {
                    JSONObject user = new JSONObject(body);
                    UdeskHelper.init(context,
                            user.getString("domain"), user.getString("appKey"), user.getString("appId"));

                    String uid = user.getString("uid");
                    String name = user.optString("name");
                    String email = user.optString("email");
                    String cell = user.optString("cell");
                    String desc = user.optString("desc");
                    UdeskHelper.setUserInfo(uid, name, email, cell, desc);
                    UdeskHelper.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case CMD_ALIPAY_PAY:
                alipayCmdID=cmdid;
                ExternalCall.makeInstance(MainActivity.myActivity).payV2(body);
                break;
            default:
                break;
        }
    }

    private void sendErrorMessage(String message) {
        OkHttpUtils.post().url(API.FEEDBACK).addParams("bid", Utils.getPackageName())
                .addParams("text", message)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(context, "发送日志失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.e(TAG, "发送日志成功" + response);

            }
        });
    }
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressWarnings("unused")
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    @SuppressWarnings("unchecked")

//                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
//                    /**
//                     对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
//                     */
//                    String resultInfo = payResult.getResult();// 同步返回需要验证的信息
//                    String resultStatus = payResult.getResultStatus();

//                    // 判断resultStatus 为9000则代表支付成功
                            Map<String, String> payResult=(Map<String, String>) msg.obj;
                    if (payResult!=null)
                    {

                        for (String key : payResult.keySet()) {
                            if (TextUtils.equals(key, "resultStatus")) {
                                ExternalCall.resultStatus = payResult.get(key);
                            } else if (TextUtils.equals(key, "result")) {
                                ExternalCall.resultInfo = payResult.get(key);
                            }
                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                JSONObject result=new JSONObject();
                                result.put("code",ExternalCall.resultStatus);
//                                result.put("info",ExternalCall.resultInfo);
                                ExternalCall.makeInstance(MainActivity.myActivity).messageCallback(result.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }

                    break;
                }
                default:
                    break;
            }
        };
    };
    /**
     * 支付宝支付业务
     *
     * @param
     */
    public void payV2(final String str) {

        Runnable payRunnable = new Runnable() {
            String payInfo=str;
            @Override
            public void run() {
                PayTask alipay = new PayTask(MainActivity.myActivity);
                Map<String, String> result = alipay.payV2(payInfo, true);
                Log.i("msp", result.toString());

                Message msg = new Message();
                msg.what = SDK_PAY_FLAG;
                msg.obj = result;
                mHandler.sendMessage(msg);
            }
        };

        Thread payThread = new Thread(payRunnable);
        payThread.start();
    }
    private void shareAliPay(String s) {

    }

    @Override
    public void onReceived() {
        WXHelper.sharePicToWechat(Utils.getScreenBitmap(), 400, isTimeline);
        if (isTimeline) {
            isTimeline = false;
        }
    }

}
