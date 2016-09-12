package com.andrew.timetracker.views.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.ISimpleCallback;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.database.dbHelper;
import com.andrew.timetracker.utils.actionsHelper;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.MainActivityTabFragment;
import com.andrew.timetracker.views.time.TimelineEditDialogFragment;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 15.08.2016.
 */
public class HomeFragment extends MainActivityTabFragment {

	private static final String TAG = "tt: HomeFragment";

	private static final int REQUEST_EDIT_TIMELINE = 1;
	private static final String DIALOG_EDIT_TIMELINE = "dialog_edit_timeline";

	boolean mIsStarted;
	Task mTask;
	Timeline mTimeline;
	Date mDateWorkStarted;
	int mSpentToday = 0; // seconds, without current
	int mTaskSpentToday = 0; // seconds, without current
	int mInactiveTotal = 0; // seconds, without current

	Button mStartStopButton;
	View mStatusView;
	TextView mSpentTimeTodayTextView;
	TextView mStartWorkingTextView;
	TextView mCurrentTaskTextView;
	TextView mCurrentTaskTimeTextView;
	TextView mInactiveTotalTextView;
	TextView mInactiveCurrentTextView;

	Spinner spinnerRecentTasks;
	boolean isSpinnerRecentTasksReady;
	RecentTasksAdapter adapterRecentTasks;
	Button btnStartRecentTask;

	View containerLastTimeline;
	ImageButton btnLastTimelineEdit, btnLastTimelineDelete;
	TextView txtLastTimeline;

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

		mStartStopButton = (Button) v.findViewById(R.id.fragment_home_start_button);
		mStatusView = v.findViewById(R.id.fragment_home_view_status);
		mSpentTimeTodayTextView = (TextView) v.findViewById(R.id.fragment_home_spent_time_today);
		mCurrentTaskTextView = (TextView) v.findViewById(R.id.fragment_home_current_task);
		mCurrentTaskTimeTextView = (TextView) v.findViewById(R.id.fragment_home_task_time);
		mInactiveTotalTextView = (TextView) v.findViewById(R.id.fragment_home_inactive_total);
		mInactiveCurrentTextView = (TextView) v.findViewById(R.id.fragment_home_inactive_current);
		mStartWorkingTextView = (TextView) v.findViewById(R.id.fragment_home_start_work);

		containerLastTimeline = v.findViewById(R.id.last_timeline_container);
		btnLastTimelineEdit = (ImageButton) v.findViewById(R.id.button_edit_last_timeline);
		btnLastTimelineDelete = (ImageButton) v.findViewById(R.id.button_delete_last_timeline);
		txtLastTimeline = (TextView) v.findViewById(R.id.last_timeline_text);

		mStartStopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onStartStop();
			}
		});

		adapterRecentTasks = new RecentTasksAdapter(getContext());
		spinnerRecentTasks = (Spinner) v.findViewById(R.id.spinner_start_recent);
		spinnerRecentTasks.setAdapter(adapterRecentTasks);
		btnStartRecentTask = (Button) v.findViewById(R.id.button_start_recent_task);
		btnStartRecentTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doStartRecentTask();
			}
		});
		spinnerRecentTasks.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (isSpinnerRecentTasksReady && position > 0){
					isSpinnerRecentTasksReady = false;
					Log.d(TAG, "recent tasks spinner - selected " + position);
					startTask(adapterRecentTasks.getItem(position));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.d(TAG, "recent tasks spinner - nothing selected");
			}
		});

		btnLastTimelineDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doDeleteCurrentTimeline();
			}
		});
		btnLastTimelineEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doEditCurrentTimeline();
			}
		});

		updateData();

		return v;
	}

	private void doEditCurrentTimeline() {
		FragmentManager manager = getFragmentManager();
		TimelineEditDialogFragment dialog = TimelineEditDialogFragment.newInstance(mTimeline.getId());
		dialog.setTargetFragment(this, REQUEST_EDIT_TIMELINE);
		dialog.show(manager, DIALOG_EDIT_TIMELINE);
	}

	private void doDeleteCurrentTimeline() {
		actionsHelper.deleteTimeline(getContext(), timelineDao(), mTimeline.getId(), new ISimpleCallback() {
			@Override
			public void onActionComplete() {
				updateData();
			}
		});
	}

	private void startTask(Task task) {
		mStartStopButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		if (mIsStarted) {
			mTimeline.setStopTime(new Date());
			mTimeline.update();
		}
		mTask = task;
		mTimeline = new Timeline(null, mTask.getId(), new Date(), null);
		timelineDao().insert(mTimeline);
		updateData();
		postDbChange();
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

	private void doStartRecentTask() {
		isSpinnerRecentTasksReady = false;
		List<Task> tasks = mTask == null ? null : dbHelper.getRecentTasks(timelineDao(), taskDao(), mTask.getId(), 20);
		if (mTask != null && tasks.size() > 0 ){
			adapterRecentTasks.setData(tasks);
			int tasksCount = tasks.size();
			spinnerRecentTasks.setSelection(0);
			spinnerRecentTasks.performClick();
			isSpinnerRecentTasksReady = true;
		} else {
			Toast.makeText(getContext(), R.string.toast_no_recent_tabs, Toast.LENGTH_SHORT).show();
		}
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
			timelineDao().insert(mTimeline);
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

		mTimeline = timelineDao().queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		mIsStarted = mTimeline != null;
		if (mIsStarted) {
			mTask = taskDao().load(mTimeline.getTaskId());
		} else {
			mTimeline = timelineDao().queryBuilder()
					  .where(TimelineDao.Properties.StopTime.isNotNull())
					  .orderDesc(TimelineDao.Properties.StopTime).limit(1).unique();
			mTask = mTimeline == null ? null : taskDao().load(mTimeline.getTaskId());
		}

		// today
		Date today = helper.getToday().getTime();

		// spent today
		mSpentToday = 0;
		mTaskSpentToday = 0;
		mInactiveTotal = 0;
		mDateWorkStarted = null;
		List<Timeline> tt = timelineDao().queryBuilder()
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

		updateUI();

		ensureTimer();
	}

	private void updateUI() {
		Log.d(TAG, "updateUI");

		mStatusView.setBackgroundResource(mIsStarted ? R.drawable.started_circle : R.drawable.stopped_circle);

		mStartStopButton.setText(mIsStarted ? R.string.button_stop : R.string.button_start);
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

		mCurrentTaskTimeTextView.setVisibility(mTask == null ? View.GONE : View.VISIBLE);
		if (mTask == null) {
			mCurrentTaskTextView.setText(R.string.home_tab_task_not_selected);
		} else {
			mCurrentTaskTextView.setText(helper.underlineText(mTask.getName()));
			mCurrentTaskTextView.setTypeface(null, Typeface.BOLD);
		}

		containerLastTimeline.setVisibility(mTimeline != null ? View.VISIBLE : View.GONE);
		if (mTimeline != null) {
			String timelineText = helper.formatTimelinePeriod(mTimeline, false, getContext())
					  + " [ " + helper.formatSpentTime(getContext(), mTimeline.getSpentSeconds(), false) + " ]";
			txtLastTimeline.setText(timelineText);
		}

		updateUI_current();
	}

	private void updateUI_current() {
		if (mTask == null) return;
		String s = String.format(getString(R.string.home_tab_task_time_today),
				  helper.formatSpentTime(getContext(), mTaskSpentToday + getStartedTaskTime(), false));
		if (mIsStarted) {
			s += ", " + String.format(getString(R.string.home_tab_task_time_current),
					  helper.formatSpentTime(getContext(), getStartedTaskTime(), true));
		}
		mCurrentTaskTimeTextView.setText(s);
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
		// TODO: hardcoded index of home tab
		if (getActivityMain().getCurrentTabIndex() == 0) {
			shouldInvalidate = false;
			updateData();
		}
	}

	Task notSelectableTask = new Task();

	class RecentTasksAdapter extends ArrayAdapter<Task> {

		public void setData(Collection<? extends Task> tasks) {
			clear();
			add(notSelectableTask);
			if (tasks != null){
				super.addAll(tasks);
			}
			notifyDataSetChanged();
		}

		public RecentTasksAdapter(Context context) {
			super(context, android.R.layout.simple_spinner_dropdown_item);
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = super.getView(position, convertView, parent);

			TextView txt = (TextView)v.findViewById(android.R.id.text1);
			txt.setText("");

			return v;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {

			if (position == 0) {
				TextView tv = new TextView(getContext());
				tv.setHeight(0);
				tv.setVisibility(View.GONE);
				return tv;
			}

			View v = super.getDropDownView(position, null, parent);

			TextView txt = (TextView)v.findViewById(android.R.id.text1);
			Task task = getItem(position);
			txt.setText(task.getName());

			return v;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EDIT_TIMELINE) {
			if (resultCode != Activity.RESULT_OK) return;
			updateData();
		}
	}
}
