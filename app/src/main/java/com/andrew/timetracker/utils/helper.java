package com.andrew.timetracker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

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

	public static String formatShortTime(Date d, boolean showSeconds) {
		return String.format(showSeconds ? "%1$tH:%1$tM" : "%1$tH:%1$tM:%1$tS", d);
	}

	public static String formatShortSpentTime(Context context, int seconds, boolean showMinutes, boolean showSeconds) {
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


	public static String formatShortSpentTime(Context context, int seconds, boolean showSeconds) {
		return formatShortSpentTime(context, seconds, true, showSeconds);
	}

	public static String formatShortSpentTime(Context context, int seconds) {
		return formatShortSpentTime(context, seconds, true, true);
	}

	public static int convertDipToPx(int dp, Context context) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
		return (int) px;
	}
}
