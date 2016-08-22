package com.andrew.timetracker.views.time;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andrew.timetracker.App;
import com.andrew.timetracker.R;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.MainActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 19.08.2016.
 */
public class TimeFragment extends Fragment implements MainActivity.ITab {

	TasksList mTasksList;
	TextView mTitle;
	ImageButton mPrevButton;
	ImageButton mNextButton;
	Button mDayButton;
	Button mWeekButton;
	Button mMonthButton;

	Calendar mCurrentDay;
	TasksList.PeriodType mPeriodType;

	private TaskDao taskDao;
	private TimelineDao timelineDao;

	@Override
	public void onTabSelected() {
		updateData();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_time, container, false);

		// DATABASE
		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		taskDao = daoSession.getTaskDao();
		timelineDao = daoSession.getTimelineDao();


		mCurrentDay = helper.getToday();
		mPeriodType = TasksList.PeriodType.DAY;

		mTasksList = (TasksList) v.findViewById(R.id.fragment_time_tasks_list);
		mTasksList.initControl(true, taskDao, timelineDao);

		mPrevButton = (ImageButton) v.findViewById(R.id.fragment_time_prev_button);
		mNextButton = (ImageButton) v.findViewById(R.id.fragment_time_next_button);
		mDayButton = (Button) v.findViewById(R.id.fragment_time_day_button);
		mWeekButton = (Button) v.findViewById(R.id.fragment_time_week_button);
		mMonthButton = (Button) v.findViewById(R.id.fragment_time_month_button);

		mDayButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setDayView();
			}
		});
		mWeekButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setWeekView();
			}
		});
		mMonthButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setMonthView();
			}
		});

		mPrevButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeDate(true);
			}
		});
		mNextButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeDate(false);
			}
		});

		mTitle = (TextView) v.findViewById(R.id.fragment_time_title);
		mTitle.setTypeface(null, Typeface.BOLD_ITALIC);

		updateData();

		return v;
	}

	private void changeDate(boolean isPrev) {
		switch (mPeriodType){
			case DAY: mCurrentDay.add(Calendar.DAY_OF_MONTH, isPrev ? -1 : 1); break;
			case WEEK: mCurrentDay.add(Calendar.WEEK_OF_MONTH, isPrev ? -1 : 1); break;
			case MONTH: mCurrentDay.add(Calendar.MONTH, isPrev ? -1 : 1); break;
		}
		updateData();
	}

	private void setDayView() {
		mPeriodType = TasksList.PeriodType.DAY;
		updateData();
	}

	private void setWeekView() {
		mPeriodType = TasksList.PeriodType.WEEK;
		updateData();
	}

	private void setMonthView() {
		mPeriodType = TasksList.PeriodType.MONTH;
		updateData();
	}

	private void updateData() {

		mDayButton.setVisibility(mPeriodType == TasksList.PeriodType.DAY ? View.GONE : View.VISIBLE);
		mWeekButton.setVisibility(mPeriodType == TasksList.PeriodType.WEEK ? View.GONE : View.VISIBLE);
		mMonthButton.setVisibility(mPeriodType == TasksList.PeriodType.MONTH ? View.GONE : View.VISIBLE);

		Date dateFrom, dateTo;
		dateFrom = dateTo = mCurrentDay.getTime();
		Calendar cal = (Calendar) mCurrentDay.clone();

		switch (mPeriodType){
			case DAY:
				dateFrom = mCurrentDay.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 1);
				dateTo = cal.getTime();
				mTitle.setText(String.format("%1$tb %1$td %1$ta", mCurrentDay));
				break;
			case WEEK:
				helper.toStartOfWeek(cal);
				dateFrom = cal.getTime();
				helper.toEndOfWeek(cal);
				dateTo = cal.getTime();
				mTitle.setText(String.format("%1$tb %1$td %1$ta - %2$tb %2$td %2$ta", dateFrom, cal));
				break;
			case MONTH:
				helper.toStartOfMonth(cal);
				dateFrom = cal.getTime();
				cal.add(Calendar.MONTH, 1);
				dateTo = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, -1);
				mTitle.setText(String.format("%1$tB (%1$td %1$ta - %2$td %2$ta)", dateFrom, cal));
				break;
		}

		List<Timeline> timelines = timelineDao.queryBuilder().where(TimelineDao.Properties.StartTime.ge(dateFrom))
				  .where(TimelineDao.Properties.StartTime.lt(dateTo))
				  .orderAsc(TimelineDao.Properties.StartTime)
				  .list();

		mTasksList.setData(timelines, mPeriodType);

	}
}
