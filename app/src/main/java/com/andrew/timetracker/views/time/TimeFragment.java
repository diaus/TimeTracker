package com.andrew.timetracker.views.time;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

		mTasksList = (TasksList) v.findViewById(R.id.fragment_time_tasks_list);

		updateData();

		return v;
	}

	private void updateData() {
		Calendar calToday = helper.getToday();
		Date today = calToday.getTime();
		calToday.add(Calendar.DAY_OF_MONTH, 1);
		Date todayEnd = calToday.getTime();

		List<Timeline> timelines = timelineDao.queryBuilder().where(TimelineDao.Properties.StartTime.ge(today))
				  .where(TimelineDao.Properties.StartTime.lt(todayEnd))
				  .orderAsc(TimelineDao.Properties.StartTime)
				  .list();

		mTasksList.setData(TasksList.PeriodType.DAY, timelines, taskDao, timelineDao);

	}
}
