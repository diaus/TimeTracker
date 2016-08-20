package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.utils.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 20.08.2016.
 */
public abstract class TimeListBase<TItemViewHolder> extends LinearLayout implements View.OnClickListener {

	protected boolean mIsTop;
	protected TaskDao mTasksDao;
	protected TimelineDao mTimelineDao;
	protected Map<Long, Task> mTasks;

	protected List<Timeline> mTimelines;
	protected Task mSelectedTask;

	protected abstract void createViews();

	protected abstract void onItemClick(ViewGroup v, TItemViewHolder holder);

	@Override
	public void onClick(View v) {
		onItemClick((LinearLayout)v.findViewById(R.id.time_list_container), (TItemViewHolder) v.getTag());
	}

	protected View inflateItem(int itemLayouId, TItemViewHolder holder) {
		View v = inflate(getContext(), itemLayouId, null);
		v.setTag(holder);
		v.setOnClickListener(this);
		return v;
	}

	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, Map<Long, Task> tasks)
	{
		mIsTop = isTop;
		mTasksDao = taskDao;
		mTimelineDao = timelineDao;
		mTasks = tasks == null ? new HashMap<Long, Task>() : tasks;

		setOrientation(VERTICAL);
		ViewGroup.LayoutParams params = this.getLayoutParams();
		if (params == null || !(params instanceof ViewGroup.MarginLayoutParams)){
			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		if (!isTop){
			((ViewGroup.MarginLayoutParams)params).setMargins(helper.convertDipToPx(10, getContext()), 0, 0, 0);
		}
		setLayoutParams(params);
	}

	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao)
	{
		initControl(isTop, taskDao, timelineDao, new HashMap<Long, Task>());
	}

	public void setData(List<Timeline> timelines, Task selectedTask)
	{
		mTimelines = timelines;
		mSelectedTask = selectedTask;

		this.removeAllViews();

		createViews();
	}

	public void setData(List<Timeline> timelines)
	{
		setData(timelines, null);
	}

	public TimeListBase(Context context) {
		super(context);
	}

	public TimeListBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TimeListBase(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TimeListBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	protected Task getTask(Long taskId) {
		if (mTasks.containsKey(taskId)) {
			return mTasks.get(taskId);
		} else {
			Task task = mTasksDao.load(taskId);
			mTasks.put(taskId, task);
			return task;
		}
	}

}
