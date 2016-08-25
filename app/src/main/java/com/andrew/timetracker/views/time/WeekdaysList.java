package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.utils.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 22.08.2016.
 */
public class WeekdaysList extends TimeListBase<Date, WeekdaysList.ItemHolder> {

	private static final String TAG = "tt: WeekdaysList";

	class ItemHolder extends TimeListBase.ItemHolder implements Comparable<ItemHolder> {
		public Date day;
		public int timeSpent;

		public ItemHolder(Date day) {
			this.day = day;
		}

		@Override
		public int compareTo(ItemHolder another) {
			return day.compareTo(another.day);
		}
	}

	@Override
	protected void createViews() {
		Context context = getContext();

		List<ItemHolder> infos = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		Date day;

		for (Timeline tl : mTimelines) {
			cal.setTime(tl.getStartTime());
			helper.truncateCalendar(cal);
			day = cal.getTime();
			ItemHolder info;
			if (mItemHolders.containsKey(day)) {
				info = mItemHolders.get(day);
			} else {
				info = new ItemHolder(day);
				mItemHolders.put(day, info);
				infos.add(info);
			}
			info.timeSpent += tl.getSpentSeconds();
		}

		Collections.sort(infos);

		List<View> titles = new ArrayList<>();
		int maxTitleWidth = 0;
		for (ItemHolder info : infos) {
			View v = inflateItem(R.layout.time_weekdays_item, info);

			TextView title = (TextView) v.findViewById(R.id.time_weekdays_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_weekdays_item_time);

			String sTitle = String.format("%1$tA (%1$tb %1$td)", info.day);
			title.setText(sTitle);

			time.setText(helper.formatShortSpentTime(context, info.timeSpent, true, false));
			time.setTypeface(null, Typeface.BOLD_ITALIC);

			this.addView(v);

			title.measure(0, 0);
			titles.add(title);
			int w = title.getMeasuredWidth();
			if (w > maxTitleWidth) maxTitleWidth = w;
		}
		for (View titleView : titles) {
			ViewGroup.LayoutParams params = titleView.getLayoutParams();
			params.width = maxTitleWidth;
			titleView.setLayoutParams(params);
		}
	}

	@Override
	protected TimeListBase createChild(ItemHolder holder) {
		TimeListBase c = mSelectedTask == null ? new TasksList(getContext()) : new TimelinesList(getContext());
		c.initControl(false, mTasksDao, mTimelineDao, mTasks, mEventHandler);

		List<Timeline> timelines = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		for (Timeline tl : mTimelines){
			cal.setTime(tl.getStartTime());
			helper.truncateCalendar(cal);
			if (holder.day.equals(cal.getTime())){
				timelines.add(tl);
			}
		}

		if (mSelectedTask == null){
			((TasksList)c).setData(timelines, TasksList.PeriodType.DAY);
		} else {
			c.setData(timelines, mSelectedTask);
		}

		return c;
	}

	public WeekdaysList(Context context) {
		super(context);
	}

	public WeekdaysList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WeekdaysList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public WeekdaysList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

}
