package com.andrew.timetracker.utils;

import android.content.Context;

import com.andrew.timetracker.R;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrew on 18.08.2016.
 */
public class helper {
	public  static Calendar getToday() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.MILLISECOND, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		return today;
	}

	public static int diffDates(Date dateFrom, Date dateTo){
		if (dateTo == null) dateTo = new Date();
		return (int) ((dateTo.getTime() - dateFrom.getTime())/1000);
	}

	public static String formatShortTime(Context context, int seconds, boolean showMinutes, boolean showSeconds) {
		int minutes = seconds / 60;
		seconds %= 60;
		int hours = minutes / 60;
		minutes %= 60;
		int days = hours / 24;
		hours %= 24;

		String s = "";
		if (days > 0){
			s += days + context.getString(R.string.day_short);
		}
		if (!showMinutes && minutes >= 30) hours += 1;
		if (hours > 0){
			if (s.length() > 0) s += " ";
			s += hours + context.getString(R.string.hour_short);
		}
		if (!showMinutes) return s;

		if (!showSeconds && seconds >= 30) minutes += 1;
		if (minutes > 0){
			if (s.length() > 0) s += " ";
			s += minutes + context.getString(R.string.minute_short);
		}
		if (!showSeconds) return s;

		if (seconds > 0){
			if (s.length() > 0) s += " ";
			s += seconds + context.getString(R.string.second_short);
		}

		if (s.length() == 0) s = "0";

		return s;
	}

	public static String formatShortTime(Context context, int seconds, boolean showSeconds) {
		return formatShortTime(context, seconds, true, showSeconds);
	}

	public static String formatShortTime(Context context, int seconds) {
		return formatShortTime(context, seconds, true, true);
	}
}
