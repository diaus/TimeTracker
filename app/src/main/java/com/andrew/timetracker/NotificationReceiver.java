package com.andrew.timetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.andrew.timetracker.database.TimelineDao;

/**
 * Created by andrew on 06.09.2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("NotificationReceiver", "got it!");
		Context appContext = context.getApplicationContext();
		if (appContext instanceof App){
			App app = (App)appContext;
			app.updateNotification(true);
		} else {
			// TODO (when?) start application
		}
	}
}
