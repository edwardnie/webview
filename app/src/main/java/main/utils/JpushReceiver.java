package main.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.jpush.android.api.JPushInterface;
import main.MainActivity;

public class JpushReceiver extends BroadcastReceiver {


	public void onReceive(Context context, Intent intent) {
		if(JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())){
			Intent i = new Intent(context, MainActivity.class);
        	//i.putExtras(bundle);
        	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP );
        	context.startActivity(i);
		}
	}

}
