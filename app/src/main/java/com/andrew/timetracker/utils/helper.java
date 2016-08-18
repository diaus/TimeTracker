package com.andrew.timetracker.utils;

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
}
