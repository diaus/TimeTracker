package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.utils.helper;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by andrew on 19.08.2016.
 */
public class TasksList extends FrameLayout {

	private LinearLayout mContainer;

	private List<Timeline> mTimelines;
	private Map<Long, Task> mTasks = new HashMap<>();

	private TimelineDao mTimelineDao;
	private TaskDao mTasksDao;

	public enum PeriodType {
		DAY, WEEK, MONTH
	}

	public TasksList(Context context) {
		super(context);
		initView(context);
	}

	public TasksList(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.time_tasks, this, true);
		mContainer = (LinearLayout) findViewById(R.id.time_tasks_container);
	}

	public void setData(PeriodType periodType, List<Timeline> timelines, TaskDao taskDao, TimelineDao timelineDao) {
		mTasksDao = taskDao;
		mTimelineDao = timelineDao;
		mContainer.removeAllViews();
		mTimelines = timelines;

		switch (periodType) {
			case DAY:
				initDay();
				break;
		}
	}

	class TaskInfo implements Comparable<TaskInfo> {
		public Long taskId;
		public int timeSpent;

		public TaskInfo(Long taskId) {
			this.taskId = taskId;
			timeSpent = 0;
		}

		public TaskInfo(Long taskId, int timeSpent) {
			this.taskId = taskId;
			this.timeSpent = timeSpent;
		}

		@Override
		public int compareTo(TaskInfo another) {
			return timeSpent > another.timeSpent ? 1 : (timeSpent < another.timeSpent ? -1 : 0);
		}
	}

	private Task getTask(Long taskId) {
		if (mTasks.containsKey(taskId)) {
			return mTasks.get(taskId);
		} else {
			Task task = mTasksDao.load(taskId);
			mTasks.put(taskId, task);
			return task;
		}
	}

	private void initDay() {
		Context context = getContext();

		Map<Long, TaskInfo> mapInfo = new HashMap<>();
		List<TaskInfo> infos = new ArrayList<>();

		int timeSpentTotal = 0;

		for (Timeline tl : mTimelines) {
			Long taskId = tl.getTaskId();
			timeSpentTotal += tl.getSpentSeconds();
			TaskInfo info;
			if (mapInfo.containsKey(taskId)) {
				info = mapInfo.get(taskId);
			} else {
				info = new TaskInfo(taskId);
				mapInfo.put(taskId, info);
				infos.add(info);
			}
			info.timeSpent += tl.getSpentSeconds();
		}

		Collections.sort(infos, Collections.reverseOrder());
		infos.add(0, new TaskInfo((long) -1, timeSpentTotal));

		List<View> titles = new ArrayList<>();
		int maxTitleWidth = 0;
		for (TaskInfo info : infos) {
			View v = inflate(context, R.layout.time_tasks_item, null);

			TextView title = (TextView) v.findViewById(R.id.time_tasks_item_title);
			TextView time = (TextView) v.findViewById(R.id.time_tasks_item_time);

			if (info.taskId == -1) {
				title.setText(context.getString(R.string.time_total));
				title.setTypeface(null, Typeface.BOLD);
			} else {
				title.setText(getTask(info.taskId).getName());
			}

			time.setText(helper.formatShortTime(context, info.timeSpent));

			mContainer.addView(v);

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


}
