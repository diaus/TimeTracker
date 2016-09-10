package com.andrew.timetracker.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.events.DbChangesEvent;
import com.andrew.timetracker.views.tasks.TaskEditDialogFragment;

import org.greenrobot.eventbus.EventBus;

import java.text.Collator;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 18.08.2016.
 */
public class helper {

	public static Collator collator = Collator.getInstance();

	public static Calendar getToday() {
		Calendar today = Calendar.getInstance();
		truncateCalendar(today);
		return today;
	}

	public static void truncateCalendar(Calendar cal) {
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
	}

	public static Date getTruncatedDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		truncateCalendar(cal);
		return cal.getTime();
	}

	public static int diffDates(Date dateFrom, Date dateTo) {
		if (dateTo == null) dateTo = new Date();
		return (int) ((dateTo.getTime() - dateFrom.getTime()) / 1000);
	}

	public static int diffDates(Calendar dateFrom, Calendar dateTo) {
		Date dt = dateTo == null ? new Date() : dateTo.getTime();
		return (int) ((dt.getTime() - dateFrom.getTime().getTime()) / 1000);
	}

	public static String formatShortTime(Date d, boolean showSeconds) {
		return String.format(showSeconds ? "%1$tH:%1$tM:%1$tS" : "%1$tH:%1$tM", d);
	}

	public static String formatSpentTime(Context context, int seconds, boolean showSeconds) {
		int minutes = seconds / 60;
		seconds %= 60;
		if (!showSeconds && seconds >= 30) minutes += 1;
		int hours = minutes / 60;
		minutes %= 60;

		String s = "";
		if (hours > 0) {
			if (s.length() > 0) s += " ";
			s += hours + context.getString(R.string.hour_short);
		}
		if (minutes > 0) {
			if (s.length() > 0) s += " ";
			s += minutes + context.getString(R.string.minute_short);
		}
		if (showSeconds) {
			if (seconds > 0) {
				if (s.length() > 0) s += " ";
				s += seconds + context.getString(R.string.second_short);
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
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.TUESDAY:
				cal.add(Calendar.DAY_OF_MONTH, -1);
				break;
			case Calendar.WEDNESDAY:
				cal.add(Calendar.DAY_OF_MONTH, -2);
				break;
			case Calendar.THURSDAY:
				cal.add(Calendar.DAY_OF_MONTH, -3);
				break;
			case Calendar.FRIDAY:
				cal.add(Calendar.DAY_OF_MONTH, -4);
				break;
			case Calendar.SATURDAY:
				cal.add(Calendar.DAY_OF_MONTH, -5);
				break;
			case Calendar.SUNDAY:
				cal.add(Calendar.DAY_OF_MONTH, -6);
				break;
		}
	}

	public static void toStartOfNextWeek(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
				cal.add(Calendar.DAY_OF_MONTH, 7);
				break;
			case Calendar.TUESDAY:
				cal.add(Calendar.DAY_OF_MONTH, 6);
				break;
			case Calendar.WEDNESDAY:
				cal.add(Calendar.DAY_OF_MONTH, 5);
				break;
			case Calendar.THURSDAY:
				cal.add(Calendar.DAY_OF_MONTH, 4);
				break;
			case Calendar.FRIDAY:
				cal.add(Calendar.DAY_OF_MONTH, 3);
				break;
			case Calendar.SATURDAY:
				cal.add(Calendar.DAY_OF_MONTH, 2);
				break;
			case Calendar.SUNDAY:
				cal.add(Calendar.DAY_OF_MONTH, 1);
				break;
		}
	}

	public static void toStartOfMonth(Calendar cal) {
		cal.set(Calendar.DAY_OF_MONTH, 1);
	}

	public static void toEndOfMonth(Calendar cal) {
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
	}

	public static String formatTimelinePeriod(Timeline tl, boolean showSeconds, Context context) {
		return helper.formatShortTime(tl.getStartTime(), showSeconds) + " - " +
				  (tl.getStopTime() == null ? context.getString(R.string.timeline_now) : helper.formatShortTime(tl.getStopTime(), showSeconds));
	}

	public static SpannableString underlineText(String text) {
		SpannableString content = new SpannableString(text);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		return content;
	}

	public static String getActivityText(Context context, List<Timeline> timelines) {
		if (timelines == null || timelines.size() == 0) return null;
		int inactiveTime = 0;
		Date dateLast = null;
		for (Timeline tl : timelines) {
			if (dateLast != null) {
				inactiveTime += diffDates(dateLast, tl.getStartTime());
			}
			dateLast = tl.getStopTime();
		}
		return String.format(context.getString(R.string.fragment_time_activity_info), formatShortTime(timelines.get(0).getStartTime(), false), formatSpentTime(context, inactiveTime, false));
	}

	public static void alert(Context context, @StringRes int titleResourceId) {
		new AlertDialog.Builder(context)
				  .setTitle(titleResourceId)
				  .setIcon(R.drawable.icon_alert)
				  .setPositiveButton(android.R.string.ok, null)
				  .show();
	}

	public static void postDbChange(Object sender) {
		EventBus.getDefault().post(new DbChangesEvent(sender));
	}

}
