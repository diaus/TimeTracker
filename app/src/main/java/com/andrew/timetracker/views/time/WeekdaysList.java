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
import com.andrew.timetracker.settings.Settings;
import com.andrew.timetracker.utils.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by andrew on 22.08.2016.
 */
public class WeekdaysList extends TimeListBase<Date, WeekdaysList.ItemHolder> {

	private static final String TAG = "tt: WeekdaysList";

	@Override
	protected Date getItemStateHolderKey(Long key) {
		return new Date(key);
	}

	class ItemHolder extends TimeListBase.ItemHolder {
		public Date day;
		public int timeSpent;
		public List<Timeline> timelines = new ArrayList<>();

		public ItemHolder(Date day) {
			this.day = day;
		}
	}

	int mMaxWidth1, mMaxWidth2;

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
			info.timelines.add(tl);
		}

		mMaxWidth1 = mMaxWidth2 = 0;
		for (ItemHolder info : infos) {
			View v = inflateItem(R.layout.time_weekdays_item, info);

			TextView title = (TextView) v.findViewById(R.id.time_weekdays_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_weekdays_item_time);

			String sTitle = String.format(Locale.getDefault(), "%1$tA (%1$tb %1$td)", info.day);
			title.setText(sTitle);

			time.setText(helper.formatSpentTime(context, info.timeSpent, false));
			time.setTypeface(null, Typeface.BOLD_ITALIC);

			if (Settings.getShowDayStartAndInactive()){
				if (mSelectedTask == null || (mParentOptions != null && mParentOptions.isTheOnlyTask)){
					TextView activityInfo = (TextView) v.findViewById(R.id.time_weekdays_item_activity_info);
					activityInfo.setVisibility(VISIBLE);
					activityInfo.setText(helper.getActivityText(getContext(), info.timelines));
				}
			}

			this.addView(v);

			time.measure(0, 0);
			int w = time.getMeasuredWidth();
			if (w > mMaxWidth1) mMaxWidth1 = w;
			title.measure(0, 0);
			w = title.getMeasuredWidth();
			if (w > mMaxWidth2) mMaxWidth2 = w;
		}
	}

	@Override
	protected TimeListBase createChild(ItemHolder holder) {
		TimeListBase c = mSelectedTask == null ? new TasksList(getContext()) : new TimelinesList(getContext());
		c.initControl(false, mTasksDao, mTimelineDao, mTasks, mEventHandler, mParentOptions);

		if (mSelectedTask == null){
			((TasksList)c).setData(holder.timelines, TasksList.PeriodType.DAY);
		} else {
			c.setData(holder.timelines, mSelectedTask);
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

	@Override
	protected boolean fixLayout() {
		return fixLayoutCommon(mMaxWidth2, mMaxWidth1 + 10, R.id.time_weekdays_item_title, R.id.time_weekdays_item_time);
	}

}
