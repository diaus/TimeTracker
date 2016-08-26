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

import java.io.InvalidClassException;
import java.io.Serializable;
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
		boolean isAutoOpenMode();
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
	ParentOptions mParentOptions;

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

	protected Long getItemStateKey(TItemHolderKey key) {
		if (key instanceof Long){
			return (Long)key;
		} else if (key instanceof Date){
			return ((Date) key).getTime();
		} else {
			return null;
		}
	}

	protected TItemHolderKey getItemStateHolderKey(Long key) {
		return (TItemHolderKey) key;
	}

	public List<TimeListBaseItemState> getOpenedState() {
		if (mItemHolders == null) return null; // for sure
		List<TimeListBaseItemState> state = new ArrayList<>();
		for (Map.Entry<TItemHolderKey, TItemHolder> entry : mItemHolders.entrySet())
		{
			ItemHolder holder = entry.getValue();
			if (holder.childList != null){
				state.add(new TimeListBaseItemState(getItemStateKey(entry.getKey()), holder.childList.getOpenedState()));
			}
		}
		return state;
	}

	public void restoreOpenedState(List<TimeListBaseItemState> state) {
		if (state == null || mItemHolders == null) return; // for sure
		Map<TItemHolderKey, Object> opened = new HashMap<>();
		// open
		for (TimeListBaseItemState itemState : state){
			TItemHolderKey holderKey = getItemStateHolderKey(itemState.key);
			opened.put(holderKey, null);
			TItemHolder holder = mItemHolders.get(holderKey);
			if (holder != null) {
				if (holder.view != null && holder.childList == null){
					onClick(holder.view);
				}
				if (holder.childList != null){
					holder.childList.restoreOpenedState(itemState.childState);
				}
			}
		}
		// close
		for (Map.Entry<TItemHolderKey, TItemHolder> p : mItemHolders.entrySet()){
			TItemHolderKey key = p.getKey();
			TItemHolder holder = p.getValue();
			if (holder.childList != null && !opened.containsKey(key)){
				ViewGroup container = (ViewGroup) holder.view.findViewById(R.id.time_list_container);
				container.removeView(holder.childList);
				holder.childList = null;
			}
		}
	}

	protected View inflateItem(int itemLayouId, TItemHolder holder) {
		View v = inflate(getContext(), itemLayouId, null);
		v.setTag(holder);
		v.setOnClickListener(this);
		holder.view = v;
		return v;
	}

	protected class ParentOptions {
		public boolean isTheOnlyTask;

		public ParentOptions(boolean isTheOnlyTask) {
			this.isTheOnlyTask = isTheOnlyTask;
		}
	}


	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, Map<Long, Task> tasks, IEventHandler eventHandler, ParentOptions options)
	{
		mIsTop = isTop;
		mTasksDao = taskDao;
		mTimelineDao = timelineDao;
		mTasks = tasks == null ? new HashMap<Long, Task>() : tasks;
		mEventHandler = eventHandler;
		mParentOptions = options;

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


	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, IEventHandler eventHandler, ParentOptions options)
	{
		initControl(isTop, taskDao, timelineDao, new HashMap<Long, Task>(), eventHandler, options);
	}

	public void initControl(boolean isTop, TaskDao taskDao, TimelineDao timelineDao, IEventHandler eventHandler)
	{
		initControl(isTop, taskDao, timelineDao, new HashMap<Long, Task>(), eventHandler, null);
	}

	public void setData(List<Timeline> timelines, Task selectedTask)
	{
		mTimelines = timelines;
		mSelectedTask = selectedTask;

		this.removeAllViews();
		mItemHolders = new HashMap<>();

		createViews();
	}

	public void setData(List<Timeline> timelines)
	{
		setData(timelines, null);
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
}
