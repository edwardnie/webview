package main.utils;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by yons on 17/4/14.
 */

public class UiUtil {

    public static void showToast(final Activity activity, final int resId) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public static void showToastLong(final Activity activity, final int resId) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, resId, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static void showToast(final Activity activity, final String text) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public static void showToastLong(final Activity activity, final String text) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

}
