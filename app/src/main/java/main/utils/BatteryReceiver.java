package main.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import main.ExternalCall;


public class BatteryReceiver extends BroadcastReceiver {
    private int _cmdid;

    public BatteryReceiver(int cmdid) {
        _cmdid = cmdid;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        JSONObject data = new JSONObject();

        try {
            data.put("level", intent.getExtras().getInt("level"));
            ExternalCall.sendMessageToGame(_cmdid, data.toString());
            context.unregisterReceiver(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}