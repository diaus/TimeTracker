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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 20.08.2016.
 */
public abstract class TimeListBase<TItemHolderKey, TItemHolder extends TimeListBase.ItemHolder> extends LinearLayout implements View.OnClickListener {

	protected IEventHandler mEventHandler;

	public interface IEventHandler {
		void invalidate();
		void editTimeline(Timeline timeline);
	}

	protected abstract void createViews();
	protected abstract TimeListBase createChild(TItemHolder holder);

	protected Map<TItemHolderKey, TItemHolder> mItemHolders;
	protected List<Timeline> mTimelines;
	protected Task mSelectedTask;

	protected boolean mIsTop;
	protected TaskDao mTasksDao;
	protected TimelineDao mTimelineDao;
	protected Map<Long, Task> mTasks;

	@Override
	public void onClick(View v) {
		ViewGroup container = (ViewGroup) v.findViewById(R.id.time_list_container);
		TItemHolder holder = (TItemHolder) v.getTag();
		if (holder.childList == null){
			holder.childList = createChild(holder);
			container.addView(holder.childList);
		} else {
			container.removeView(holder.childList);
			holder.childList = null;
		}
	}

	protected class ItemHolder {
		public View view;
		public TimeListBase childList;
	}

	protected class ItemState {
		public TItemHolderKey key;
		public Object childState;

		public ItemState(TItemHolderKey key, Object childState) {
			this.childState = childState;
			this.key = key;
		}
	}

	public Object getOpenedState() {
		if (mItemHolders == null) return null; // for sure
		List<ItemState> state = new ArrayList<>();
		for (Map.Entry<TItemHolderKey, TItemHolder> entry : mItemHolders.entrySet())
		{
			ItemHolder holder = entry.getValue();
			if (holder.childList != null){
				state.add(new ItemState(entry.getKey(), holder.childList.getOpenedState()));
			}
		}
		return state;
	}

	public void restoreOpenedState(Object state) {
		if (state == null || mItemHolders == null) return; // for sure
		List<ItemState> tasksState = (List<ItemState>) state;
		for (ItemState itemState : tasksState){
			TItemHolder holder = mItemHolders.get(itemState.key);
			if (holder != null && holder.view != null && holder.childList == null){
				onClick(holder.view);
				holder.childList.restoreOpenedState(itemState.childState);
			}
		}
	}

	protected View inflateItem(int itemLayouId, TItemHolder holder) {
		View v = inflate(getContext(), itemLayouId, null);
		v.setTag(holder);
		v.setOnClickListener(this);
		return v;
	}

	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, Map<Long, Task> tasks, IEventHandler eventHandler)
	{
		mIsTop = isTop;
		mTasksDao = taskDao;
		mTimelineDao = timelineDao;
		mTasks = tasks == null ? new HashMap<Long, Task>() : tasks;
		mEventHandler = eventHandler;

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

	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, IEventHandler eventHandler)
	{
		initControl(isTop, taskDao, timelineDao, new HashMap<Long, Task>(), eventHandler);
	}

	public void setData(List<Timeline> timelines, Task selectedTask)
	{
		mTimelines = timelines;
		mSelectedTask = selectedTask;

		this.removeAllViews();
		mItemHolders = null;

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

	public Task getTask(Long taskId) {
		if (mTasks.containsKey(taskId)) {
			return mTasks.get(taskId);
		} else {
			Task task = mTasksDao.load(taskId);
			mTasks.put(taskId, task);
			return task;
		}
	}

}
