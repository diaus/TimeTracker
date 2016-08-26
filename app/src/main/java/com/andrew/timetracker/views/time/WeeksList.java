package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.utils.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrew on 22.08.2016.
 */
public class WeeksList extends TimeListBase<Date, WeeksList.ItemHolder> {
	private static final String TAG = "tt: WeeksList";

	class ItemHolder extends TimeListBase.ItemHolder {
		public Date day;
		public int timeSpent;

		public ItemHolder(Date day) {
			this.day = day;
		}
	}

	@Override
	protected void createViews() {
		Context context = getContext();

		List<ItemHolder> infos = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		Date day;
		Date monthStart = null;
		Date monthNext = null;

		for (Timeline tl : mTimelines) {
			cal.setTime(tl.getStartTime());
			helper.truncateCalendar(cal);
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			day = cal.getTime();

			if (monthStart == null){
				cal.setTime(tl.getStartTime());
				helper.truncateCalendar(cal);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				monthStart = cal.getTime();
				cal.add(Calendar.MONTH, 1);
				cal.add(Calendar.DAY_OF_MONTH, -1);
				monthNext = cal.getTime();
			}

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

		List<View> titles = new ArrayList<>();
		int maxTitleWidth = 0;
		for (ItemHolder info : infos) {
			View v = inflateItem(R.layout.time_weeks_item, info);

			TextView title = (TextView) v.findViewById(R.id.time_weeks_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_weeks_item_time);

			Date weekFrom = info.day;
			if (weekFrom.before(monthStart)) weekFrom = monthStart;
			cal.setTime(info.day);
			helper.toEndOfWeek(cal);
			Date weekTo = cal.getTime();
			if (weekTo.after(monthNext)) weekFrom = monthNext;
			String sTitle = String.format(Locale.getDefault(), "%1$ta %1$td - %2$ta %2$td", weekFrom, weekTo);
			title.setText(sTitle);

			time.setText(helper.formatSpentTime(context, info.timeSpent, false));
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
		TimeListBase c = mSelectedTask == null ? new TasksList(getContext()) : new WeekdaysList(getContext());
		c.initControl(false, mTasksDao, mTimelineDao, mTasks, mEventHandler, mParentOptions);

		Date weekFrom = holder.day;
		Calendar cal = Calendar.getInstance();
		cal.setTime(weekFrom);
		cal.add(Calendar.WEEK_OF_MONTH, 1);
		Date weekTo = cal.getTime();
		Date d;

		List<Timeline> timelines = new ArrayList<>();
		for (Timeline tl : mTimelines){
			d = tl.getStartTime();
			helper.truncateCalendar(cal);
			if ((d.equals(weekFrom) || d.after(weekFrom)) && d.before(weekTo)){
				timelines.add(tl);
			}
		}

		if (mSelectedTask == null){
			((TasksList)c).setData(timelines, TasksList.PeriodType.WEEK);
		} else {
			c.setData(timelines, mSelectedTask);
		}

		return c;
	}

	public WeeksList(Context context) {
		super(context);
	}

	public WeeksList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WeeksList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public WeeksList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
}
