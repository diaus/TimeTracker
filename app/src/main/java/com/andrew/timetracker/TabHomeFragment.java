package com.andrew.timetracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 15.08.2016.
 */
public class TabHomeFragment extends Fragment implements MainActivity.ITab {

	private TimelineDao timelineDao;
	private TaskDao taskDao;

	boolean mIsStarted;
	Date mDateStarted;
	Task mTask;
	Timeline mTimeline;
	int mSpentToday = 0;

	Button mStartButton;
	View mStatusView;
	TextView mSpentTimeTodayTextView;
	TextView mCurrentTaskTextView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tab_home, container, false);

		// DATABASE
		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		timelineDao = daoSession.getTimelineDao();
		taskDao = daoSession.getTaskDao();

		mStartButton = (Button) v.findViewById(R.id.fragment_tab_home_start_button);
		mStatusView = v.findViewById(R.id.fragment_tab_home_view_status);
		mSpentTimeTodayTextView = (TextView) v.findViewById(R.id.fragment_tab_home_spent_time_today);
		mCurrentTaskTextView = (TextView) v.findViewById(R.id.fragment_tab_home_current_task);

		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onStartStop();
			}
		});

		// delete invalid timelines
//		timelineDao.queryBuilder().where(new WhereCondition.StringCondition(TimelineDao.Properties.TaskId.columnName + " NOT IN (SELECT "
//				  + TaskDao.Properties.Id.columnName + " FROM " + TaskDao.TABLENAME + ")")).buildDelete().executeDeleteWithoutDetachingEntities();

		updateData();

		return v;
	}

	private void onStartStop() {
		if (mTask == null) return;

		if (mIsStarted){
			mTimeline.setStopTime(new Date());
			mTimeline.update();
		} else {
			Timeline timeline = new Timeline(null, mTask.getId(), new Date(), null);
			timelineDao.insert(timeline);
		}

		updateData();
	}

	private void updateData() {

		mTimeline = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		mIsStarted = mTimeline != null;
		if (mIsStarted){
			mDateStarted = mTimeline.getStartTime();
			mTask = taskDao.load(mTimeline.getTaskId());
		} else {
			Timeline timeline = timelineDao.queryBuilder().orderDesc(TimelineDao.Properties.StopTime).limit(1).unique();
			mTask = timeline == null ? null : taskDao.load(timeline.getTaskId());
		}

		// spent today
		Calendar today = Calendar.getInstance();
		today.set(Calendar.MILLISECOND, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		List<Timeline> tt = timelineDao.queryBuilder()
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.gt(today.getTime())).list();
		mSpentToday = 0;
		for (Timeline tl : tt) {
			mSpentToday += (tl.getStopTime().getTime() - tl.getStartTime().getTime())/1000;
		}

		updateUI();
	}


	private void updateUI() {
		mStatusView.setBackgroundResource(mIsStarted ? R.drawable.started_circle : R.drawable.stopped_circle);

		mStartButton.setText(mIsStarted ? R.string.stop_button : R.string.start_button);
		mStartButton.setBackgroundResource(mTask == null ? R.drawable.inactive_circle : (mIsStarted ? R.drawable.stopped_circle : R.drawable.started_circle));

		int time = (mSpentToday + getStartedTaskTime())/60;
		mSpentTimeTodayTextView.setText(String.format(getString(R.string.home_tab_spent_time_today), time / 60, time % 60));

		if (mTask == null){
			mCurrentTaskTextView.setText(R.string.home_tab_task_not_selected);
		} else {
			mCurrentTaskTextView.setText(String.format(getString(R.string.home_tab_task_title), mTask.getName()));
		}
	}


	private int getStartedTaskTime() {
		if (!mIsStarted) return 0;
		return (int) ((System.currentTimeMillis() - mDateStarted.getTime())/1000);
	}

	@Override
	public void onTabSelected() {
		updateData();
	}
}
