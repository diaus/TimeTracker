package com.andrew.timetracker.views;

import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.TimelineDao;

/**
 * Created by andrew on 18.08.2016.
 */
public interface IMainActivity {
	void switchToHomeTab();
	int getCurrentTabIndex();
	TaskDao getTaskDao();
	TimelineDao getTimelineDao();
	void switchToTasksTab(Long taskId);
}
