package com.andrew.timetracker.views.home;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andrew.timetracker.App;
import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.OnDoubleTouchListener;
import com.andrew.timetracker.commons.SimpleListView;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.IMainActivity;
import com.andrew.timetracker.views.MainActivity;
import com.andrew.timetracker.views.MainActivityTabFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew on 15.08.2016.
 */
public class HomeFragment extends MainActivityTabFragment {

	private static final String TAG = "tt: HomeFragment";

	private TimelineDao timelineDao;
	private TaskDao taskDao;

	boolean mIsStarted;
	Task mTask;
	Timeline mTimeline;
	Date mDateWorkStarted;
	int mSpentToday = 0; // seconds, without current
	int mTaskSpentToday = 0; // seconds, without current
	int mInactiveTotal = 0; // seconds, without current
	List<Task> mRecentTasks = new ArrayList<>();
	int mSelectedRecentTaskPosition = -1;

	Button mStartStopButton;
	View mStatusView;
	TextView mSpentTimeTodayTextView;
	TextView mStartWorkingTextView;
	TextView mCurrentTaskTextView;
	TextView mCurrentTaskTimeTodayTextView;
	TextView mCurrentTaskTimeCurrentTextView;
	TextView mInactiveTotalTextView;
	TextView mInactiveCurrentTextView;
	SimpleListView mRecentTasksList;
	View mRecentsContainer;

	Handler timerHandler = new Handler();
	boolean isTimerSecondStarted = false;
	Runnable timerSecond = new Runnable() {
		@Override
		public void run() {
			updateUI_current();
			timerHandler.postDelayed(this, 1000);
		}
	};
	Runnable timerMinute = new Runnable() {
		@Override
		public void run() {
			updateUI();
			timerHandler.postDelayed(this, 60000);
		}
	};

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_home, container, false);

		// DATABASE
		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		timelineDao = daoSession.getTimelineDao();
		taskDao = daoSession.getTaskDao();

		mStartStopButton = (Button) v.findViewById(R.id.fragment_home_start_button);
		mStatusView = v.findViewById(R.id.fragment_home_view_status);
		mSpentTimeTodayTextView = (TextView) v.findViewById(R.id.fragment_home_spent_time_today);
		mCurrentTaskTextView = (TextView) v.findViewById(R.id.fragment_home_current_task);
		mCurrentTaskTimeTodayTextView = (TextView) v.findViewById(R.id.fragment_home_task_time_today);
		mCurrentTaskTimeCurrentTextView = (TextView) v.findViewById(R.id.fragment_home_task_time_current);
		mInactiveTotalTextView = (TextView) v.findViewById(R.id.fragment_home_inactive_total);
		mInactiveCurrentTextView = (TextView) v.findViewById(R.id.fragment_home_inactive_current);
		mStartWorkingTextView = (TextView) v.findViewById(R.id.fragment_home_start_work);

		mStartStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onStartStop();
			}
		});

		mRecentsContainer = v.findViewById(R.id.fragment_home_recents_container);
		mRecentTasksList = (SimpleListView) v.findViewById(R.id.fragment_home_recents_list);
		mRecentTasksList.setAdapter(new SimpleListView.IAdapter<RecentTaskViewHolder>() {
			@Override
			public int getItemCount() {
				return mRecentTasks.size();
			}

			@Override
			public void bindViewHolder(RecentTaskViewHolder holder, int position) {
				holder.bindItem(position);
			}

			@Override
			public RecentTaskViewHolder createViewHolder(View v) {
				return new RecentTaskViewHolder(v);
			}
		}, R.layout.home_recent_task_item);

		// delete timelines with invalid taskId
//		timelineDao.queryBuilder().where(new WhereCondition.StringCondition(TimelineDao.Properties.TaskId.columnName + " NOT IN (SELECT "
//				  + TaskDao.Properties.Id.columnName + " FROM " + TaskDao.TABLENAME + ")")).buildDelete().executeDeleteWithoutDetachingEntities();

		updateData();

		return v;
	}

	class RecentTaskViewHolder implements View.OnClickListener {
		int mPosition;
		View mView;
		TextView mTaskTitle;
		ImageButton mStartButton;

		public RecentTaskViewHolder(View v) {
			mView = v;
			mTaskTitle = (TextView) v.findViewById(R.id.home_recent_task_item_title);
			v.setOnClickListener(this);
			mStartButton = (ImageButton) v.findViewById(R.id.home_recent_task_item_start_button);
			mStartButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startRecentTask();
				}
			});
			v.setOnTouchListener(new OnDoubleTouchListener(getContext(), false) {
				@Override
				protected boolean onDoubleTap() {
					startRecentTask();
					return true;
				}
			});
		}

		private void startRecentTask() {
			mStartButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
			if (mIsStarted) {
				mTimeline.setStopTime(new Date());
				mTimeline.update();
			}
			mTask = mRecentTasks.get(mPosition);
			mTimeline = new Timeline(null, mTask.getId(), new Date(), null);
			timelineDao.insert(mTimeline);
			updateData();
			postDbChange();
		}

		public void bindItem(int position) {
			mPosition = position;
			Task task = mRecentTasks.get(position);
			mTaskTitle.setText(task.getName());
			updateSelected();
		}

		public void updateSelected() {
			boolean isSelected = mSelectedRecentTaskPosition == mPosition;
			mView.setBackgroundResource(isSelected ? R.drawable.home_recent_task_selected_bg : 0);
			mStartButton.setVisibility(isSelected ? View.VISIBLE : View.GONE);
		}

		@Override
		public void onClick(View v) {
			if (mSelectedRecentTaskPosition == mPosition) return;
			int prevSelected = mSelectedRecentTaskPosition;
			mSelectedRecentTaskPosition = mPosition;
			if (prevSelected != -1) {
				mRecentTasksList.updateItem(prevSelected);
			}
			updateSelected();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		stopTimer();
		timerHandler.removeCallbacks(timerMinute);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		ensureTimer();
		timerHandler.removeCallbacks(timerMinute);
		timerHandler.postDelayed(timerMinute, 60000);
		updateUI();
	}

	private void ensureTimer() {
		if (mIsStarted) {
			startTimer();
		} else {
			stopTimer();
		}
	}

	private void startTimer() {
		if (!isTimerSecondStarted) {
			timerHandler.postDelayed(timerSecond, 1000);
			isTimerSecondStarted = true;
		}
	}

	private void stopTimer() {
		if (isTimerSecondStarted) {
			timerHandler.removeCallbacks(timerSecond);
			isTimerSecondStarted = false;
		}
	}

	private void onStartStop() {
		if (mTask == null) return;

		mStartStopButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

		if (mIsStarted) {
			mTimeline.setStopTime(new Date());
			mTimeline.update();
			int time = mTimeline.getSpentSeconds();
			mSpentToday += time;
			mTaskSpentToday += time;
		} else {
			if (mTimeline != null && mTimeline.getStopTime().after(helper.getToday().getTime())) {
				mInactiveTotal += helper.diffDates(mTimeline.getStopTime(), null);
			}
			mTimeline = new Timeline(null, mTask.getId(), new Date(), null);
			timelineDao.insert(mTimeline);
			if (mDateWorkStarted == null) {
				mDateWorkStarted = mTimeline.getStartTime();
			}
		}

		mIsStarted = !mIsStarted;

		updateUI();
		ensureTimer();
		postDbChange();
	}

	private void updateData() {
		Log.d(TAG, "updateData");

		mTimeline = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		mIsStarted = mTimeline != null;
		if (mIsStarted) {
			mTask = taskDao.load(mTimeline.getTaskId());
		} else {
			mTimeline = timelineDao.queryBuilder()
					  .where(TimelineDao.Properties.StopTime.isNotNull())
					  .orderDesc(TimelineDao.Properties.StopTime).limit(1).unique();
			mTask = mTimeline == null ? null : taskDao.load(mTimeline.getTaskId());
		}

		// today
		Date today = helper.getToday().getTime();

		// spent today
		mSpentToday = 0;
		mTaskSpentToday = 0;
		mInactiveTotal = 0;
		mDateWorkStarted = null;
		List<Timeline> tt = timelineDao.queryBuilder()
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.gt(today))
				  .orderAsc(TimelineDao.Properties.StartTime).list();
		Date lastStopped = null;
		for (Timeline tl : tt) {
			int time = tl.getSpentSeconds();
			mSpentToday += time;
			if (tl.getTaskId().equals(mTask.getId())) {
				mTaskSpentToday += time;
			}
			if (lastStopped != null) {
				mInactiveTotal += helper.diffDates(lastStopped, tl.getStartTime());
			} else {
				mDateWorkStarted = tl.getStartTime();
			}
			lastStopped = tl.getStopTime();
		}
		if (mIsStarted && lastStopped != null) {
			mInactiveTotal += helper.diffDates(lastStopped, mTimeline.getStartTime());
		}

		if (mDateWorkStarted == null && mIsStarted) {
			mDateWorkStarted = mTimeline.getStartTime();
		}

		// recent tasks
		tt = timelineDao.queryBuilder()
				  .where(TimelineDao.Properties.TaskId.notEq(mTask != null ? mTask.getId() : -1))
				  .limit(100)
				  .orderDesc(TimelineDao.Properties.StartTime).list();
		mRecentTasks = new ArrayList<>();
		mSelectedRecentTaskPosition = -1;
		Map<Long, Object> mapRecents = new HashMap<>();
		for (int i = 0; i < tt.size(); i++) {
			Long taskId = tt.get(i).getTaskId();
			if (!mapRecents.containsKey(taskId)) {
				mapRecents.put(taskId, null);
				mRecentTasks.add(taskDao.load(taskId));
				if (mRecentTasks.size() == 3) break;
			}
		}
		mRecentsContainer.setVisibility(mRecentTasks.size() > 0 ? View.VISIBLE : View.GONE);
		mRecentTasksList.updateData();

		updateUI();

		ensureTimer();
	}


	private void updateUI() {
		Log.d(TAG, "updateUI");

		mStatusView.setBackgroundResource(mIsStarted ? R.drawable.started_circle : R.drawable.stopped_circle);

		mStartStopButton.setText(mIsStarted ? R.string.home_tab_stop_button : R.string.home_tab_start_button);
		mStartStopButton.setBackgroundResource(mTask == null ? R.drawable.inactive_circle : (mIsStarted ? R.drawable.stopped_circle : R.drawable.started_circle));

		mSpentTimeTodayTextView.setText(String.format(getString(R.string.home_tab_spent_time_today),
				  helper.formatSpentTime(getContext(), mSpentToday + getStartedTaskTime(), false)));

		int inactiveCurrent = mIsStarted || mTimeline == null || mTimeline.getStopTime().before(helper.getToday().getTime())
				  ? 0 : helper.diffDates(mTimeline.getStopTime(), null);

		mInactiveTotalTextView.setVisibility((mInactiveTotal / 60) == 0 ? View.GONE : View.VISIBLE);
		mInactiveTotalTextView.setText(String.format(getString(R.string.home_tab_inactive_total),
				  helper.formatSpentTime(getContext(), mInactiveTotal, false)));

		mStartWorkingTextView.setVisibility(mDateWorkStarted == null ? View.GONE : View.VISIBLE);
		if (mDateWorkStarted != null) {
			mStartWorkingTextView.setText(String.format(getString(R.string.home_tab_start_working), mDateWorkStarted));
		}

		mInactiveCurrentTextView.setVisibility((inactiveCurrent / 60) == 0 ? View.GONE : View.VISIBLE);
		mInactiveCurrentTextView.setText(String.format(getString(R.string.home_tab_inactive_current),
				  helper.formatSpentTime(getContext(), inactiveCurrent, false)));

		mCurrentTaskTimeTodayTextView.setVisibility(mTask == null ? View.GONE : View.VISIBLE);
		if (mTask == null) {
			mCurrentTaskTextView.setText(R.string.home_tab_task_not_selected);
		} else {
			mCurrentTaskTextView.setText(helper.underlineText(mTask.getName()));
			mCurrentTaskTextView.setTypeface(null, Typeface.BOLD);
			mCurrentTaskTimeTodayTextView.setText(String.format(getString(R.string.home_tab_task_time_today),
					  helper.formatSpentTime(getContext(), mTaskSpentToday + getStartedTaskTime(), false)));

		}

		updateUI_current();
	}

	private void updateUI_current() {
		mCurrentTaskTimeCurrentTextView.setVisibility(!mIsStarted ? View.GONE : View.VISIBLE);
		if (mIsStarted) {
			mCurrentTaskTimeCurrentTextView.setText(String.format(getString(R.string.home_tab_task_time_current),
					  helper.formatSpentTime(getContext(), getStartedTaskTime(), true)));
		}
	}

	private int getStartedTaskTime() {
		if (!mIsStarted) return 0;
		return mTimeline.getSpentSeconds();
	}

	@Override
	public void onTabSelected() {
		if (shouldInvalidate) {
			shouldInvalidate = false;
			updateData();
		}
	}

	@Override
	protected void onDbChange() {
		Log.d(TAG, "invalidate on db changes");
	}
}
