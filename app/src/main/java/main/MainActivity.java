package main;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bumptech.glide.Glide;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hlkgj.app.wxapi.WXEntryActivity;
import com.hlkgj.app.wxapi.WXHelper;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.hlkgj.app.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import layaair.game.IMarket.IPlugin;
import layaair.game.IMarket.IPluginRuntimeProxy;
import layaair.game.Market.GameEngine;
import main.utils.RecordHelper;
import main.utils.SpUtil;
import main.utils.Utils;

import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import android.webkit.JavascriptInterface;
import android.annotation.SuppressLint;
import android.content.Context;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.View.OnKeyListener;
//import android.view.View.OnClickListener;
//import android.content.DialogInterface.OnClickListener;
@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    public static IPlugin mPlugin = null;
    public static Activity activity;
    private static ImageView imageView;
    boolean isExit = false;
    private FrameLayout container;
    private IPluginRuntimeProxy mProxy = null;
    public static  com.tencent.smtt.sdk.WebView webView;
    private String url="http://m.hlkgj.com/";
    public static MainActivity myActivity;
    public static void hideSplash() {
        if (imageView != null && imageView.isShown()) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.fade(imageView);
                }
            });
        }
    }

    public static void showSplash() {
        if (imageView != null) {
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public static void onLoginUi() {
        //Here we are at login screen
//        ExternalCall.makeInstance(ExternalCall.context).call(ExternalCall.CMD_START_RECORDING, 0, "", null);
//        ExternalCall.makeInstance(ExternalCall.context).call(ExternalCall.CMD_SHARE_TEXT_TO_WECHAT, 0,
//                "{\"type\": \"wechat\", \"text\": \"这是需要分享到微信的文本\"}", null);
    }
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myActivity=this;
        Log.i(TAG, "onCreateonCreateonCreateonCreateonCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        boolean isWechatInstalled = Utils.isWechatInstalled();
        Log.i(TAG, "onCreateonCreateonCreateonCreateonCreate"+isWechatInstalled);
        if (isWechatInstalled) {
            WXHelper.registerWechat(MainActivity.myActivity, new ValueCallback<JSONObject>() {
                @Override
                public void onReceiveValue(JSONObject value) {
                    Log.i(TAG, "Login: ");
                }
            });
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFormat(PixelFormat.TRANSLUCENT); // <--- This makes xperia play happy
        //全屏
//        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_main);
        activity = this;
        container = (FrameLayout) findViewById(R.id.container);
        checkDeepLink();
        initSplash();
        if (MainApp.isLoaded) {
            ProcessPhoenix.triggerRebirth(this);
        }
        checkPermission();
        mProxy = new RuntimeProxy(this);
//        mPlugin = new GameEngine(this);
//        mPlugin.game_plugin_set_runtime_proxy(mProxy);
//        String local = "http://192.168.1.102:8900/bin/h5/index.html";
//        String tz = "http://res.zj-games.com/hot/pcwz/";
//        mPlugin.game_plugin_set_option("gameUrl", tz);

//        mPlugin.game_plugin_init();
//        View view = mPlugin.game_plugin_get_view();
//        container.addView(view, 0);//index为0可以让游戏界面在splash的下层
        MainApp.isLoaded = true;
        initView();
        loadUrl(url);
//        getWindow().clearFlags(
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        ExternalCall.makeInstance(myActivity).payV2("alipay_sdk=alipay-sdk-php-20161101&app_id=2018051760086822&biz_content=%7B%22body%22%3A%22%E5%BC%80%E9%80%9A%E4%BC%9A%E5%91%98%22%2C%22subject%22%3A+%22%E5%BC%80%E9%80%9A%E4%BC%9A%E5%91%98%22%2C%22out_trade_no%22%3A+%2220180519025557710000075%22%2C%22timeout_express%22%3A+%2230m%22%2C%22total_amount%22%3A+%220.1%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%7D&charset=UTF-8&format=json&method=alipay.trade.app.pay&notify_url=http%3A%2F%2Fm.hlkgj.com%2Fapi.php&sign_type=RSA2&timestamp=2018-05-19+02%3A55%3A57&version=1.0&sign=TTTA5monecRRAOZNlQ4om2mRKnFcsoMVlCuJF%2BGHbc8mMrcom7B5RfIYRuCqqXgAGdap7VtQMma9dWM%2BSFtVzSModjVKN%2BXFOTInbOiyxI4u6lpk%2BNgHvoxlZ8ORCIqNyBkoC49sec1B8BaDq59ixmFPX1EEqNi4thjd%2F8INOGCvRhhQwwlO%2B7r11dUL%2FlKcbIrU2hsWPvhXihStQi1l3VJUJH%2BtX7QV8xTIBmpdbtHtXqDINI5JoaMLXN7494LLiNawXRMxiTakYrDa1q4kib1w1Ez8frVj9a%2FdRJt%2B7O3Zkbu43liD%2FxGpqPYSUsoSYV6Ii4iFC5841%2BjaI0z7ig%3D%3D");
    }
    private void initView() {
        View view= this.getLayoutInflater().inflate((R.layout.tbs_activity), null);
        webView = (com.tencent.smtt.sdk.WebView) view.findViewById(R.id.webView);
        container.addView(view,0);
    }
    public class JavaScriptInterface {
        Context mContext;

        JavaScriptInterface(Context c) {
            mContext = c;
        }
        @JavascriptInterface
        public void postMessage(String webMessage){
//            JSONObject  myJson = new JSONObject(webMessage);
            Log.i(TAG, "postMessagepostMessagepostMessage " + webMessage);
//            JSONObject myJson=new j
            try {
                JSONObject jsonObject = new JSONObject(webMessage);
                int cmd = jsonObject.getInt("cmd");
                int cmdid = jsonObject.getInt("cmdid");
                String body = jsonObject.getString("body");
                ExternalCall.makeInstance(MainActivity.myActivity).call(cmd,cmdid,body,new ValueCallback<JSONObject>() {
                    @Override
                    public void onReceiveValue(JSONObject value) {
                        String data;
                        if (value.has("data")) {
                            data = value.optString("data");
                        } else {
                            data = value.toString();
                        }
                        JSONObject callbackData = new JSONObject();
                        try {
                            callbackData.put("cmdid", value.getInt("cmdid"));
                            callbackData.put("data", data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //LayaPlatformCallback.GetInstance().LP_SendMessageToPlatformCallback(callbackData.toString());
                        MainActivity.myActivity.CallJS(callbackData.toString());
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public void CallJS(final String data)
    {
        final int version = Build.VERSION.SDK_INT;
        Log.i(TAG, "callJScallJScallJScallJS " + data);
// 因为该方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
        runOnUiThread(new Runnable() {
            String realData=data;
            @Override
            public void run() {
                //你的报错代码
                if (version < 18) {
                    MainActivity.webView.loadUrl("javascript:window.receiveFromNative("+realData+")");
                } else {
                    MainActivity.webView.evaluateJavascript("javascript:window.receiveFromNative("+realData+")",new com.tencent.smtt.sdk.ValueCallback<String>(){
                        @Override
                        public void onReceiveValue(String value) {
                            //此处为 js 返回的结果
                        }
                    });
                }
            }
        });

    }
    private void loadUrl(String url) {
        Log.i(TAG, "urlurlurl_____________ " + url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //  设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //若setSupportZoom是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
//        settings.setSupportZoom(true);  //支持缩放，默认为true。是setBuiltInZoomControls的前提。
//        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        webSettings.supportMultipleWindows();  //多窗口

        webSettings.setAllowFileAccess(true);  //设置可以访问文件
        webSettings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点

        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        //设置编码格式
        webSettings.setDefaultTextEncodingName("UTF-8");
        // 关于是否缩放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webSettings.setDisplayZoomControls(false);
        }

        /**
         *  Webview在安卓5.0之前默认允许其加载混合网络协议内容
         *  在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }
        webSettings.setLoadsImagesAutomatically(true);  //支持自动加载图片

        webSettings.setDomStorageEnabled(true); //开启DOM Storage

//        webSettings.setDownloadListener(new DownloadListener() {
//            @Override
//            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
//                Uri uri = Uri.parse(url);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                startActivity(intent);
//                finish();
//            }
//        });
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedError(WebView var1, int var2, String var3, String var4) {
                Log.i("打印日志","网页加载失败");
            }
        });
        //进度条
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    Log.i("打印日志","加载完成");
                }
            }
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                // 根据协议的参数，判断是否是所需要的url(原理同方式2)
                // 一般根据scheme（协议格式） & authority（协议名）判断（前两个参数）
                //假定传入进来的 url = "js://webview?arg1=111&arg2=222"（同时也是约定好的需要拦截的）

//                Uri uri = Uri.parse(message);
//                // 如果url的协议 = 预先约定的 js 协议
//                // 就解析往下解析参数
//                if ( uri.getScheme().equals("js")) {
//
//                    // 如果 authority  = 预先约定协议里的 webview，即代表都符合约定的协议
//                    // 所以拦截url,下面JS开始调用Android需要的方法
//                    if (uri.getAuthority().equals("webview")) {
//
//                        //
//                        // 执行JS所需要调用的逻辑
//                        System.out.println("js调用了Android的方法");
//                        // 可以在协议上带有参数并传递到Android上
//                        HashMap<String, String> params = new HashMap<>();
//                        Set<String> collection = uri.getQueryParameterNames();
//
//                        //参数result:代表消息框的返回值(输入值)
//                        result.confirm("js调用了Android的方法成功啦");
//                    }
//                    return true;
//                }
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

// 通过alert()和confirm()拦截的原理相同，此处不作过多讲述

            // 拦截JS的警告框
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                return super.onJsAlert(view, url, message, result);
                final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                TextView title = new TextView(view.getContext());
                title.setText("提示");
                title.setPadding(10, 10, 10, 10);
                title.setGravity(Gravity.CENTER);
// title.setTextColor(getResources().getColor(R.color.greenBG));
                title.setTextSize(20);

                TextView msg = new TextView(view.getContext());
                msg.setText(message);
                msg.setPadding(20, 10, 10, 20);
                msg.setGravity(Gravity.CENTER);
                msg.setTextSize(16);
                builder.setCustomTitle(title);
                builder.setView(msg);
                builder.setPositiveButton("确定", null);
//                builder.setTitle("提示")
//                        .setMessage(message)
//                        .setPositiveButton("确定", null);

                // 不需要绑定按键事件
                // 屏蔽keycode等于84之类的按键
//                builder.setOnKeyListener(new OnKeyListener() {
//                    public boolean onKey(android.content.DialogInterface dialog, int keyCode,KeyEvent event) {
////                        Log.v("onJsAlert", "keyCode=="   keyCode   "event="  event);
//                        return true;
//                    }
//                });
                // 禁止响应按back键的事件
                builder.setCancelable(false);

                AlertDialog dialog = builder.create();
                dialog.show();
                result.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
                return true;
                // return super.onJsAlert(view, url, message, result);
            }

            // 拦截JS的确认框
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }
        });
        final JavaScriptInterface myJavaScriptInterface
                = new JavaScriptInterface(this);
        webView.addJavascriptInterface(myJavaScriptInterface,"callNative");
    }
    @Override
    protected void onStart() {
        super.onStart();
        test();
    }


    void test() {

    }

    private void checkPermission() {
        new TedPermission(this)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {

                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> arrayList) {

                    }
                })
                .setDeniedMessage(R.string.permission_hint)
                .setDeniedCloseButtonText("拒绝")
                .setGotoSettingButtonText("去设置")
                .setPermissions(Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    private void initSplash() {
        imageView = new ImageView(this);
        Glide.with(this).load(R.raw.splash).into(imageView);
        container.addView(imageView);
    }

    private void checkDeepLink() {
        Uri uri = getIntent().getData();
        if (uri != null) {
            Log.i(TAG, "checkDeepLink: Uri " + uri.toString());
            String uriString = uri.toString();
            SpUtil.save("uri", uriString);
//            String channel = uri.getQueryParameter("channel");
//            String data = uri.getQueryParameter("data");
        }
    }

    protected void onPause() {
        super.onPause();
//        if (MainApp.isLoaded) mPlugin.game_plugin_onPause();
    }

    protected void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        if (MainApp.isLoaded) mPlugin.game_plugin_onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: I' back!");
        ExternalCall.sendMessageToGame(SpUtil.getInt("cmdid"), ExternalCall.MSG_RETURN_GAME);
    }

    protected void onDestroy() {
        RecordHelper.clearCache();
        super.onDestroy();
//        if (MainApp.isLoaded) mPlugin.game_plugin_onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK&& webView.canGoBack()) {
            Hook();
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 对于好多应用，会在程序中杀死 进程，这样会导致我们统计不到此时Activity结束的信息，
    // 对于这种情况需要调用 'MobclickAgent.onKillProcess( Context )'
    // 方法，保存一些页面调用的数据。正常的应用是不需要调用此方法的。
    private void Hook() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(this, "在按一次退出程序", Toast.LENGTH_SHORT).show();
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);
        } else {
            //   MobclickAgent.onKillProcess(MainActivity.this);
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }
    }
}
