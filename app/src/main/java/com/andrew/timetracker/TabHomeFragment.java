package com.andrew.timetracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.UnderlineSpan;
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
import com.andrew.timetracker.utils.helper;

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
	int mSpentToday = 0; // seconds, without current
	int mTaskSpentToday = 0; // seconds, without current
	int mInactiveTotal = 0; // seconds, without current

	Button mStartButton;
	View mStatusView;
	TextView mSpentTimeTodayTextView;
	TextView mStartWorkingTextView;
	TextView mCurrentTaskTextView;
	TextView mCurrentTaskTimeInfoTextView;
	TextView mCurrentTaskStartedInfoTextView;
	TextView mInactiveTotalTextView;
	TextView mInactiveCurrentTextView;

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
		mCurrentTaskTimeInfoTextView = (TextView) v.findViewById(R.id.fragment_tab_home_task_time_info);
		mCurrentTaskStartedInfoTextView = (TextView) v.findViewById(R.id.fragment_tab_home_task_started_info);
		mInactiveTotalTextView = (TextView) v.findViewById(R.id.fragment_tab_home_inactive_total);
		mInactiveCurrentTextView = (TextView) v.findViewById(R.id.fragment_tab_home_inactive_current);
		mStartWorkingTextView = (TextView) v.findViewById(R.id.fragment_tab_home_start_work);

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

		if (mIsStarted) {
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
		if (mIsStarted) {
			mDateStarted = mTimeline.getStartTime();
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
		List<Timeline> tt = timelineDao.queryBuilder()
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.gt(today))
				  .orderAsc(TimelineDao.Properties.StartTime).list();
		Date lastStopped = null;
		for (Timeline tl : tt) {
			int time = tl.getSpentSeconds();
			mSpentToday += time;
			if (tl.getTaskId() == mTask.getId()) {
				mTaskSpentToday += time;
			}
			if (lastStopped != null){
				mInactiveTotal += helper.diffDates(lastStopped, tl.getStartTime());
			}
			lastStopped = tl.getStopTime();
		}
		if (mIsStarted){
			mInactiveTotal += helper.diffDates(lastStopped, mTimeline.getStartTime());
		}

		updateUI();
	}


	private void updateUI() {
		mStatusView.setBackgroundResource(mIsStarted ? R.drawable.started_circle : R.drawable.stopped_circle);

		mStartButton.setText(mIsStarted ? R.string.stop_button : R.string.start_button);
		mStartButton.setBackgroundResource(mTask == null ? R.drawable.inactive_circle : (mIsStarted ? R.drawable.stopped_circle : R.drawable.started_circle));

		int time = (mSpentToday + getStartedTaskTime()) / 60;
		mSpentTimeTodayTextView.setText(String.format(getString(R.string.home_tab_spent_time_today), time / 60, time % 60));

		int inactiveCurrent = mIsStarted || mTimeline == null || mTimeline.getStopTime().before(helper.getToday().getTime())
				  ? 0 : helper.diffDates(mTimeline.getStopTime(), null) / 60;

		time = (mInactiveTotal / 60 ) + inactiveCurrent;
		mInactiveTotalTextView.setVisibility(time == 0 ? View.GONE : View.VISIBLE);
		mInactiveTotalTextView.setText(String.format(getString(R.string.home_tab_inactive_total), time / 60, time % 60));

		mInactiveCurrentTextView.setVisibility(inactiveCurrent == 0 ? View.GONE : View.VISIBLE);
		mInactiveCurrentTextView.setText(String.format(getString(R.string.home_tab_inactive_current), inactiveCurrent / 60, inactiveCurrent % 60));

		mCurrentTaskTimeInfoTextView.setVisibility(mTask == null ? View.GONE : View.VISIBLE);
		mCurrentTaskStartedInfoTextView.setVisibility(mTask == null ? View.GONE : View.VISIBLE);
		mStartWorkingTextView.setVisibility(!mIsStarted && mSpentToday == 0 ? View.GONE : View.VISIBLE);
		if (mTask == null) {
			mCurrentTaskTextView.setText(R.string.home_tab_task_not_selected);
		} else {
			SpannableString content = new SpannableString(mTask.getName());
			content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
			mCurrentTaskTextView.setText(content);

			int today = (mTaskSpentToday + getStartedTaskTime()) / 60;
			int current = getStartedTaskTime();
			if (mIsStarted){
				mCurrentTaskTimeInfoTextView.setText(String.format(getString(R.string.home_tab_task_time_info_started),
						  today / 60, today % 60, current / 3600, (current / 60) % 60, current % 60));
			} else {
				mCurrentTaskTimeInfoTextView.setText(String.format(getString(R.string.home_tab_task_time_info_stopped),
						  today / 60, today % 60));
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(mTimeline.getStartTime());
			String lastTime = String.format("%1$tH:%1$tM:%1$tS", cal);
			if (helper.getToday().after(cal)) {
				lastTime = String.format("%1$ta,%1$tb,%1$td ", cal) + lastTime;
			}
			mCurrentTaskStartedInfoTextView.setText(String.format(getString(R.string.home_tab_task_started_info), lastTime));

			mStartWorkingTextView.setText(String.format(getString(R.string.home_tab_start_working), mTimeline.getStartTime()));
		}
	}


	private int getStartedTaskTime() {
		if (!mIsStarted) return 0;
		return helper.diffDates(mDateStarted, null);
	}

	@Override
	public void onTabSelected() {
		updateData();
	}
}
