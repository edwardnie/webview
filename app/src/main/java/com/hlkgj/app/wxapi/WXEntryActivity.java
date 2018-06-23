package com.hlkgj.app.wxapi;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.ValueCallback;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import main.Constants;
import main.ExternalCall;
import okhttp3.Call;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    public static final String OPENID = "openid";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRE_TIME = "expire_time";
    public static final String REFRESH_TOKEN = "refresh_token";
    private static final String TAG = "WX";
    private static int cmdid;
    private static Context context;
    private static ValueCallback<JSONObject> callback;
    private static SharedPreferences sp;

    public static void init(Context context, ValueCallback<JSONObject> callback) {
        WXEntryActivity.context = context;
        WXEntryActivity.callback = callback;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void registerCallback(int cmdid) {
        WXEntryActivity.cmdid = cmdid;
    }


    public static void doWXLogin() {
        Log.i(TAG, "doWXLogin: ");
        final SendAuth.Req loginReq = new SendAuth.Req();
        loginReq.scope = "snsapi_userinfo";
        loginReq.state = "wechat_test";
        WXHelper.getWXAPI().sendReq(loginReq);
    }


    private static void saveTokenData(String json) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        try {
            JSONObject response = new JSONObject(json);
            if (response.has("errcode")) {
                Log.i(TAG, "errcode>>>" + response.getString("errcode"));
                doWXLogin();
                return;
            }

            String expire = response.getString("expires_in");
            long expireTime = System.currentTimeMillis() + Long.valueOf(expire);

            String token = response.getString(ACCESS_TOKEN);
            String openid = response.getString(OPENID);
            requestUserInfo(token, openid);

            String refreshToken = response.getString(REFRESH_TOKEN);
            editor.putString(REFRESH_TOKEN, refreshToken);
            editor.putLong(EXPIRE_TIME, expireTime);
            editor.apply();

        } catch (JSONException e) {
            doWXLogin();
            e.printStackTrace();
        }

    }

    public static void startWechat() {
        long expireTime = sp.getLong(EXPIRE_TIME, 0);
        if (expireTime == 0) {
            doWXLogin();
        } else {
            Log.i(TAG, "startWechat: logined before, go refresh token");
            if (expireTime < System.currentTimeMillis()) {
                String refresh = sp.getString(REFRESH_TOKEN, "");
                refreshToken(refresh);
            }
        }
    }

    private static void requestUserInfo(String accessToken, String openId) {
        if ("".equals(accessToken) || "".equals(openId)) {
            doWXLogin();
        } else {
            getUserInfo(accessToken, openId);
        }
    }

    private static void getUserInfo(String token, String openid) {
        OkHttpUtils.post().url("https://api.weixin.qq.com/sns/userinfo")
                .addParams("access_token", token)
                .addParams("openid", openid)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            callback.onReceiveValue(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private static void refreshToken(String refreshToken) {
        String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" + Constants.WECHAT_APP_ID +
                "&grant_type=refresh_token&refresh_token=" + refreshToken;
        OkHttpUtils.get().url(url).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(String response, int id) {
                saveTokenData(response);
            }
        });
    }

    public static JSONObject createJson(String code) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public static JSONObject createJson(int code) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WXHelper.getWXAPI().handleIntent(getIntent(), this);
    }

    @Override
    public void onResp(BaseResp resp) {
        Log.i(TAG, ">>>on Resp>>>>" + resp.errCode);

        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    String code = ((SendAuth.Resp) resp).code;
                    callback.onReceiveValue(createJson(code));
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                    callback.onReceiveValue(createJson(""));
                    break;
            }
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            ExternalCall.sendMessageToGame(cmdid, createJson(resp.errCode));
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL:
                default:
                    break;
            }
        }
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.i(TAG, ">>>on Req>>>>");
    }


    private void getToken(String respCode) {
        OkHttpUtils.post().url(Constants.WECHAT_ACCESS_TOKEN_URL)
                .addParams("appid", Constants.WECHAT_APP_ID)
                .addParams("secret", Constants.WECHAT_APP_SECRET)
                .addParams("code", respCode)
                .addParams("grant_type", "authorization_code")
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.i(TAG, response);
                        saveTokenData(response);
                    }
                });

    }
}
