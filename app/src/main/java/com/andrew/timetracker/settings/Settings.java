package com.andrew.timetracker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by andrew on 17.08.2016.
 */
public class Settings {
	private static Settings sInstance;
	private Context mAppContext;

	public static boolean getShowDayStartAndInactive() {
		return sInstance.getBoolean("showDayStartAndInactive", false);
	}

	public static void setShowDayStartAndInactive(boolean doShow) {
		sInstance.setBoolean("showDayStartAndInactive", doShow);
	}


//	public static long getLastTaskId() {
//		return sInstance.getLong("lastTaskId", -1);
//	}
//
//	public static void setLastTaskId(long taskId) {
//		sInstance.setLong("lastTaskId", taskId);
//	}

	public static void init(Context c){
		if(sInstance != null) return;
		sInstance = new Settings(c);
	}

	private Settings(Context c) {
		mAppContext = c.getApplicationContext();
	}

	private long getLong(String key, long defaultValue)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		return pref.getLong(key, defaultValue);
	}

	private void setLong(String key, long value)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		pref.edit().putLong(key, value).apply();
	}

	private boolean getBoolean(String key, boolean defaultValue)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		return pref.getBoolean(key, defaultValue);
	}

	private void setBoolean(String key, boolean value)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		pref.edit().putBoolean(key, value).apply();
	}

}
