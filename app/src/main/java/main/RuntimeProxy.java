package main;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;

import com.hlkgj.app.wxapi.WXEntryActivity;
import com.hlkgj.app.wxapi.WXHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import main.utils.Utils;


public class RuntimeProxy implements layaair.game.IMarket.IPluginRuntimeProxy {

    private String TAG = "RuntimeProxy";
    private Activity mActivity = null;

    public RuntimeProxy(Activity mainActivity) {
        mActivity = mainActivity;
    }

    @Override
    public boolean laya_set_value(String key, Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object laya_get_value(String key) {
        Log.d(TAG, "laya_get_value key=" + key);
        String str = null;
        if (key.equalsIgnoreCase("CacheDir")) {
            return mActivity.getCacheDir().toString() + File.separator;
        }
        return null;
    }

    @Override
    public void laya_stop_game_engine() {
        // TODO Auto-generated method stub
        Log.d(TAG, "Login laya_stop_game_engine.");
    }

    @Override
    public Object laya_invoke_Method(String method, Bundle param) {
        // TODO Auto-generated method stub
        System.out.println("1111");
        return null;
    }

    @Override
    public void Login(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        boolean isWechatInstalled = Utils.isWechatInstalled();
        Log.i(TAG, "Login: ");
        if (isWechatInstalled) {
            WXHelper.registerWechat(mActivity, callback);
            WXEntryActivity.doWXLogin();
        } else {
            callback.onReceiveValue(WXEntryActivity.createJson("guest"));
        }

    }

    @Override
    public void Logout(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Logout info = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        try {
            result.put("status", 0);
            result.put("msg", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void Pay(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Logout Pay = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        try {
            result.put("status", 0);
            result.put("msg", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void PushIcon(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Logout PushIcon = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        try {
            result.put("status", 0);
            result.put("msg", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void Share(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        Log.d(TAG, "Logout Share = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        try {
            result.put("status", 0);
            result.put("msg", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void OpenBBS(JSONObject jsonObj, ValueCallback<JSONObject> callback) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Logout OpenBBS = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        try {
            result.put("status", 0);
            result.put("msg", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void GetFriendsList(JSONObject jsonObj,
                               ValueCallback<JSONObject> callback) {
        // TODO Auto-generated method stub
        Log.d(TAG, "Logout GetFriendsList = " + jsonObj.toString());
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        try {

            JSONObject p1 = new JSONObject();
            p1.put("userId", "1111111");
            p1.put("nickName", "xiaoming");
            p1.put("photo", "http://xxx.com/xxx.jpg");
            p1.put("sex", "0");

            JSONObject p2 = new JSONObject();
            p2.put("userId", "1111111");
            p2.put("nickName", "xiaoming");
            p2.put("photo", "http://xxx.com/xxx.jpg");
            p2.put("sex", "0");
            array.put(p1);
            array.put(p2);

            result.put("status", 0);
            result.put("msg", "success");
            result.put("friends", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        callback.onReceiveValue(result);
    }

    @Override
    public void SendMessageToPlatform(JSONObject jsonObj,
                                      ValueCallback<JSONObject> callback) {
        Log.d(TAG, "Logout SendMessageToPlatform = " + jsonObj.toString());
        try {
            int cmd = jsonObj.getInt("cmd");
            int cmdid = jsonObj.getInt("cmdid");
            String body = jsonObj.getString("body");
            ExternalCall.makeInstance(mActivity).call(cmd, cmdid, body, callback);

        } catch (JSONException e) {
            Log.e(TAG, "SendMessageToPlatform: no cmd or body");
            e.printStackTrace();
        }

    }

}
