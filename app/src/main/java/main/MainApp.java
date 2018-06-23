package main;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import cn.jpush.android.api.JPushInterface;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsListener;
import android.util.Log;
/**
 * Created by yons on 16/8/17.
 */
public class MainApp extends Application {
    private static final String TAG = "MainApp";
    public static boolean firstEnter = true;
    public static Context appContext;
    public static boolean isLoaded;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: init");
        JPushInterface.setDebugMode(false);
        JPushInterface.init(this);
        appContext = this;
        initTbs();
    }

    private void initTbs() {
        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
//                LogUtil.i("onViewInitFinished is " + arg0);
                Log.i(TAG, "onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };

        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
//                LogUtil.i("onDownloadFinish");
                Log.i(TAG, "onDownloadFinish");
            }

            @Override
            public void onInstallFinish(int i) {
//                LogUtil.i("onInstallFinish");
                Log.i(TAG, "onInstallFinish");
            }

            @Override
            public void onDownloadProgress(int i) {
//                LogUtil.i("onDownloadProgress:" + i);
                Log.i(TAG, "onDownloadProgress:" + i);
            }
        });

        QbSdk.initX5Environment(getApplicationContext(), cb);
    }
}
