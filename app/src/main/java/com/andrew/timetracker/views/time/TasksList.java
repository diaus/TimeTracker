package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.settings.Settings;
import com.andrew.timetracker.utils.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by andrew on 19.08.2016.
 */
public class TasksList extends TimeListBase<Long, TasksList.ItemHolder> {

	private static final String TAG = "tt: TasksList";

	public enum PeriodType {
		DAY, WEEK, MONTH
	}

	PeriodType mPeriodType;

	public void setData(List<Timeline> timelines, PeriodType periodType)
	{
		mPeriodType = periodType;
		setData(timelines);
		// open automatically
		if (mEventHandler.isAutoOpenMode() && (periodType != PeriodType.DAY || mIsTop)){
			ItemHolder holder = mItemHolders.get(mSelectedTask != null ? mSelectedTask.getId() : -1);
			if (holder != null && holder.childList == null){
				onClick(holder.view);
			}
		}
	}

	class ItemHolder extends TimeListBase.ItemHolder implements Comparable<ItemHolder> {
		public Long taskId;
		public int timeSpent;

		public ItemHolder(Long taskId) {
			this.taskId = taskId;
			timeSpent = 0;
		}

		public ItemHolder(Long taskId, int timeSpent) {
			this.taskId = taskId;
			this.timeSpent = timeSpent;
		}

		@Override
		public int compareTo(ItemHolder another) {
			return timeSpent > another.timeSpent ? 1 : (timeSpent < another.timeSpent ? -1 : 0);
		}
	}

	int mMaxWidth1, mMaxWidth2;

	@Override
	protected void createViews() {
		Context context = getContext();

		List<ItemHolder> infos = new ArrayList<>();
		int timeSpentTotal = 0;

		for (Timeline tl : mTimelines) {
			Long taskId = tl.getTaskId();
			timeSpentTotal += tl.getSpentSeconds();
			ItemHolder info;
			if (mItemHolders.containsKey(taskId)) {
				info = mItemHolders.get(taskId);
			} else {
				info = new ItemHolder(taskId);
				mItemHolders.put(taskId, info);
				infos.add(info);
			}
			info.timeSpent += tl.getSpentSeconds();
		}

		boolean isTheOnlyTask = infos.size() == 1;
		if (isTheOnlyTask){
			mSelectedTask = getTask(infos.get(0).taskId);
			mParentOptions = new ParentOptions(true);
		} else {
			mParentOptions = null;
			Collections.sort(infos, Collections.reverseOrder());
			ItemHolder totalHolder = new ItemHolder((long) -1, timeSpentTotal);
			infos.add(0, totalHolder);
			mItemHolders.put((long) -1, totalHolder);
		}

		mMaxWidth1 = mMaxWidth2 = 0;
		for (ItemHolder info : infos) {
			View v = inflateItem(R.layout.time_tasks_item, info);

			TextView title = (TextView) v.findViewById(R.id.time_tasks_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_tasks_item_time);

			if (info.taskId == -1) {
				title.setText(context.getString(R.string.time_total));
				title.setTypeface(null, Typeface.BOLD);
			} else {
				title.setText(getTask(info.taskId).getName());
				if (isTheOnlyTask){
					title.setTypeface(null, Typeface.BOLD);
				}
			}

			time.setText(helper.formatSpentTime(context, info.timeSpent, false));
			time.setTypeface(null, Typeface.BOLD_ITALIC);

			if (Settings.getShowDayStartAndInactive()) {
				if (timeSpentTotal > 0 && mPeriodType == PeriodType.DAY && mIsTop && (info.taskId == -1 || isTheOnlyTask)){
					TextView activityInfo = (TextView) v.findViewById(R.id.time_tasks_item_activity_info);
					activityInfo.setVisibility(VISIBLE);
					activityInfo.setText(helper.getActivityText(getContext(), mTimelines));
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
		TimeListBase c = null;
		switch (mPeriodType){
			case DAY: c = new TimelinesList(getContext()); break;
			case WEEK: c = new WeekdaysList(getContext()); break;
			case MONTH: c = new WeeksList(getContext()); break;
		}
		c.initControl(false, mTasksDao, mTimelineDao, mTasks, mEventHandler, mParentOptions);

		List<Timeline> timelines;
		Task task = null;
		Long taskId = holder.taskId;
		if (taskId == -1){
			timelines = mTimelines;
		} else {
			timelines = new ArrayList<>();
			for (Timeline tl : mTimelines){
				if (tl.getTaskId() == taskId){
					timelines.add(tl);
				}
			}
			task = getTask(taskId);
		}
		c.setData(timelines, task);

		return c;
	}

	public TasksList(Context context) {
		super(context);
	}

	public TasksList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TasksList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TasksList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected boolean fixLayout() {
		return fixLayoutCommon(mMaxWidth2, mMaxWidth1 + 10, R.id.time_tasks_item_title, R.id.time_tasks_item_time);
	}

}
