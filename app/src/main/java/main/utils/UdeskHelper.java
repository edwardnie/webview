package main.utils;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;

import static main.ExternalCall.context;

/**
 * Created by yons on 17/6/15.
 */

public class UdeskHelper {
    private static final String TAG = "UdeskHelper";
    private static boolean shouldInit = true;

    public static void init(Context context, String domain, String appKey, String appId) {
        if (shouldInit) {
            UdeskSDKManager.getInstance().initApiKey(context, domain, appKey, appId);
            shouldInit = false;
        }

    }

    public static void setUserInfo(String uid, String name, String email, String cell, String desc) {
        Map<String, String> info = new HashMap<>();
        info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, uid);
        info.put(UdeskConst.UdeskUserInfo.NICK_NAME, name);
        Log.d(TAG, "setUserInfo: uid " + uid + " name: " + name + " email: " + email + " cell: " + cell + desc);
        info.put(UdeskConst.UdeskUserInfo.EMAIL, email);
        info.put(UdeskConst.UdeskUserInfo.CELLPHONE, cell);
        info.put(UdeskConst.UdeskUserInfo.DESCRIPTION, desc);
        UdeskSDKManager.getInstance().setUserInfo(context, uid, info);
    }

    public static void start() {
        UdeskSDKManager.getInstance().entryChat(context);
    }

}
