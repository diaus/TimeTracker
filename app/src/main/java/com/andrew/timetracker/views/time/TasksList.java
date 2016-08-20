package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.utils.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 19.08.2016.
 */
public class TasksList extends TimeListBase<TasksList.ItemHolder> {

	private static final String TAG = "tt: TasksList";

	public enum PeriodType {
		DAY, WEEK, MONTH
	}

	PeriodType mPeriodType;

	public void setData(List<Timeline> timelines, PeriodType periodType)
	{
		mPeriodType = periodType;
		setData(timelines);
	}

	@Override
	protected void createViews() {
		switch (mPeriodType) {
			case DAY:
				initDay();
				break;
		}
	}

	@Override
	protected void onItemClick(ViewGroup v, ItemHolder holder) {
		Log.d(TAG, "clicked on " + (holder.taskId == -1 ? "Total" : getTask(holder.taskId).getName()));
		if (holder.childList == null){
			holder.childList = createChildTimelines(holder.taskId);
			v.addView(holder.childList);
		} else {
			v.removeView(holder.childList);
			holder.childList = null;
		}
	}

	private TimelinesList createChildTimelines(Long taskId) {
		TimelinesList c = new TimelinesList(getContext());
		c.initControl(false, mTasksDao, mTimelineDao, mTasks);
		if (taskId == -1){
			c.setData(mTimelines);
		} else {
			List<Timeline> timelines = new ArrayList<>();
			for (Timeline tl : mTimelines){
				if (tl.getTaskId() == taskId){
					timelines.add(tl);
				}
			}
			c.setData(timelines);
		}

		return c;
	}

	class ItemHolder implements Comparable<ItemHolder> {
		public Long taskId;
		public int timeSpent;

		public View childList;

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

	private void initDay() {
		Context context = getContext();

		Map<Long, ItemHolder> mapInfo = new HashMap<>();
		List<ItemHolder> infos = new ArrayList<>();

		int timeSpentTotal = 0;

		for (Timeline tl : mTimelines) {
			Long taskId = tl.getTaskId();
			timeSpentTotal += tl.getSpentSeconds();
			ItemHolder info;
			if (mapInfo.containsKey(taskId)) {
				info = mapInfo.get(taskId);
			} else {
				info = new ItemHolder(taskId);
				mapInfo.put(taskId, info);
				infos.add(info);
			}
			info.timeSpent += tl.getSpentSeconds();
		}

		Collections.sort(infos, Collections.reverseOrder());
		infos.add(0, new ItemHolder((long) -1, timeSpentTotal));

		List<View> titles = new ArrayList<>();
		int maxTitleWidth = 0;
		for (ItemHolder info : infos) {
			View v = inflateItem(R.layout.time_tasks_item, info);

			TextView title = (TextView) v.findViewById(R.id.time_tasks_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_tasks_item_time);

			if (info.taskId == -1) {
				title.setText(context.getString(R.string.time_total));
				title.setTypeface(null, Typeface.BOLD);
			} else {
				title.setText(getTask(info.taskId).getName());
			}

			time.setText(helper.formatShortSpentTime(context, info.timeSpent));

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
}
