package com.andrew.timetracker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by andrew on 18.08.2016.
 */
public class helper {


	public static Calendar getToday(){
		Calendar today = Calendar.getInstance();
		truncateCalendar(today);
		return today;
	}

	public static void truncateCalendar(Calendar cal){
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
	}

	public static Date getTruncatedDate(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		truncateCalendar(cal);
		return cal.getTime();
	}

	public static int diffDates(Date dateFrom, Date dateTo){
		if (dateTo == null) dateTo = new Date();
		return (int) ((dateTo.getTime() - dateFrom.getTime())/1000);
	}

	public static String formatShortTime(Date d, boolean showSeconds) {
		return String.format(showSeconds ? "%1$tH:%1$tM:%1$tS" : "%1$tH:%1$tM", d);
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
		if (showMinutes)
		{
			if (!showSeconds && seconds >= 30) minutes += 1;
			if (minutes > 0){
				if (s.length() > 0) s += " ";
				s += minutes + context.getString(R.string.minute_short);
			}
			if (showSeconds){
				if (seconds > 0){
					if (s.length() > 0) s += " ";
					s += seconds + context.getString(R.string.second_short);
				}
			}
		}

		if (s.length() == 0) s = "0";

		return s;
	}

	public static int convertDipToPx(int dp, Context context) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return (int) px;
	}

	public static void toStartOfWeek(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)){
			case Calendar.TUESDAY: cal.add(Calendar.DAY_OF_MONTH, -1); break;
			case Calendar.WEDNESDAY: cal.add(Calendar.DAY_OF_MONTH, -2); break;
			case Calendar.THURSDAY: cal.add(Calendar.DAY_OF_MONTH, -3); break;
			case Calendar.FRIDAY: cal.add(Calendar.DAY_OF_MONTH, -4); break;
			case Calendar.SATURDAY: cal.add(Calendar.DAY_OF_MONTH, -5); break;
			case Calendar.SUNDAY: cal.add(Calendar.DAY_OF_MONTH, -6); break;
		}
	}

	public static void toEndOfWeek(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)){
			case Calendar.MONDAY: cal.add(Calendar.DAY_OF_MONTH, 6); break;
			case Calendar.TUESDAY: cal.add(Calendar.DAY_OF_MONTH, 5); break;
			case Calendar.WEDNESDAY: cal.add(Calendar.DAY_OF_MONTH, 4); break;
			case Calendar.THURSDAY: cal.add(Calendar.DAY_OF_MONTH, 3); break;
			case Calendar.FRIDAY: cal.add(Calendar.DAY_OF_MONTH, 2); break;
			case Calendar.SATURDAY: cal.add(Calendar.DAY_OF_MONTH, 1); break;
		}
	}

	public static void toStartOfMonth(Calendar cal){
		cal.set(Calendar.DAY_OF_MONTH, 1);
	}

	public static void toEndOfMonth(Calendar cal){
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
	}

	public static String formatTimelinePeriod(Timeline tl, boolean showSeconds, Context context) {
		return helper.formatShortTime(tl.getStartTime(), showSeconds) + " - " +
				  (tl.getStopTime() == null ? context.getString(R.string.timeline_now) : helper.formatShortTime(tl.getStopTime(), showSeconds));
	}
}
