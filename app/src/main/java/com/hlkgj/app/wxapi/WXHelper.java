package com.hlkgj.app.wxapi;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;

import com.hlkgj.app.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import main.Constants;
import main.ExternalCall;


/**
 * Created by yons on 15/11/11.
 */
public class WXHelper {
    private static final String TAG = "WX";
    private static IWXAPI api;

    public static IWXAPI getWXAPI() {
        return api;
    }

    public static void registerWechat(Context context, ValueCallback<JSONObject> callback) {
        Log.i(TAG, "registerWechatregisterWechatregisterWechatregisterWechat"+context);
        if (api == null) {
            api = WXAPIFactory.createWXAPI(context, Constants.WECHAT_APP_ID, false);
            boolean test=api.registerApp(Constants.WECHAT_APP_ID);
            Log.i(TAG, "registerWechatregisterWechatregisterWechatregisterWechat1111111"+test);
        }
        Log.i(TAG, "registerWechatregisterWechatregisterWechatregisterWechat"+context);
        WXEntryActivity.init(context, callback);
    }

    public static void shareToWechat(String text, boolean sendToTimeline) {
        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = text;

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = sendToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        if (api == null) {
            api = WXAPIFactory.createWXAPI(ExternalCall.context, Constants.WECHAT_APP_ID, false);
            api.registerApp(Constants.WECHAT_APP_ID);
        }
        api.sendReq(req);
    }

    public static void shareLinkToWechat(String url, String title, String desc, String picPath) {
        shareLinkToWechat(url, title, desc, picPath, false);
    }

    public static void shareLinkToTimeline(String url, String title, String desc, String picPath) {
        shareLinkToWechat(url, title, desc, picPath, true);
    }


    public static void shareLinkToWechat(String url, String title, String desc, String picPath, boolean sendToTimeline) {
        Log.i(TAG, "shareLinkToWechat ");
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = desc;
        Bitmap thumb;
        if (TextUtils.isEmpty(picPath)) {
            thumb = BitmapFactory.decodeResource(ExternalCall.context.getResources(), R.drawable.share);
        } else {
            thumb = BitmapFactory.decodeFile(picPath);
        }

        Log.i(TAG, "sharePicToWechat: thumbData bytes: " + thumb.getByteCount());
        msg.thumbData = compressBitmapToByteArray(thumb, true);
        Log.i(TAG, "sharePicToWechat: thumbData compressed bytes length " + msg.thumbData.length);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = sendToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        if (api == null) {
            api = WXAPIFactory.createWXAPI(ExternalCall.context, Constants.WECHAT_APP_ID, false);
            api.registerApp(Constants.WECHAT_APP_ID);
        }
        api.sendReq(req);
    }

    public static void sharePicToWechat(Bitmap bmp, int thumbMax, boolean sendToTimeline) {
//        String pic = Environment.getExternalStorageDirectory().getAbsolutePath() + "/texasholdem/share.png";
//        Bitmap bmp = BitmapFactory.decodeFile(picPath);
        WXImageObject imgObj = new WXImageObject(getScaledBitmap(bmp, bmp.getWidth() * 9 / 16));
        //坑啊，bitmap对象不能太大！

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        msg.thumbData = compressBitmapToByteArray(getScaledBitmap(bmp, thumbMax), true);  // 设置缩略图

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = sendToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        if (api == null) {
            api = WXAPIFactory.createWXAPI(ExternalCall.context, Constants.WECHAT_APP_ID, false);
            api.registerApp(Constants.WECHAT_APP_ID);
        }
        api.sendReq(req);
    }

    public static Bitmap getScaledBitmap(Bitmap bmp, int thumbMax) {
        Log.i(TAG, "getByteCount: " + bmp.getByteCount());
        if (thumbMax < 100) thumbMax = 100;

        double w = bmp.getWidth();
        double h = bmp.getHeight();
        double ww = thumbMax;
        double hh = thumbMax;

        if (w > h) hh = thumbMax / w * h;
        else ww = thumbMax / h * w;

        Log.i(TAG, "width: " + ww + " height: " + hh);
        return Bitmap.createScaledBitmap(bmp, (int) ww, (int) hh, true);
    }

    public static void sharePicToWechat(String picPath, int scaleSize, boolean sendToTimeline) {
//        String pic = Environment.getExternalStorageDirectory().getAbsolutePath() + "/texasholdem/share.png";
        Bitmap bmp = BitmapFactory.decodeFile(picPath);
        WXImageObject imgObj = new WXImageObject(bmp);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, scaleSize, scaleSize, true);
        bmp.recycle();
        msg.thumbData = compressBitmapToByteArray(thumbBmp, true);  // 设置缩略图

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = sendToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;

        if (api == null) {
            api = WXAPIFactory.createWXAPI(ExternalCall.context, Constants.WECHAT_APP_ID, false);
            api.registerApp(Constants.WECHAT_APP_ID);
        }
        api.sendReq(req);
    }


    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    public static byte[] compressBitmapToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "compressBitmapToByteArray: " + result.length);
        return result;
    }

    public static Bitmap compressBitmap(final Bitmap bmp, final boolean needRecycle) {
        Log.d(TAG, "compressBitmap: before " + bmp.getByteCount());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(result));
        Log.d(TAG, "compressBitmap: after " + bitmap.getByteCount());
        return bitmap;
    }

    public static void StartPay(JSONObject json) {
        try {
            if (api == null) {
                api = WXAPIFactory.createWXAPI(ExternalCall.context, Constants.WECHAT_APP_ID, false);
                api.registerApp(Constants.WECHAT_APP_ID);
            }
            PayReq req = new PayReq();
            //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
            req.appId = json.getString("appid");
            req.partnerId = json.getString("partnerid");
            req.prepayId = json.getString("prepayid");
            req.nonceStr = json.getString("noncestr");
            req.timeStamp = json.getString("timestamp");
            req.packageValue = json.getString("package");
            req.sign = json.getString("sign");
//        req.extData			= "app data"; // optional
//        Toast.makeText(PayActivity.this, "正常调起支付", Toast.LENGTH_SHORT).show();
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            Log.i(TAG, "StartPay" + json.toString());
            api.sendReq(req);
        } catch (Exception e) {
            Log.e("PAY_GET", "异常：" + e.getMessage());
        }
    }

    public static void goWechat() {
        PackageManager packageManager = ExternalCall.context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.tencent.mm");
        ExternalCall.context.startActivity(intent);
    }
}
