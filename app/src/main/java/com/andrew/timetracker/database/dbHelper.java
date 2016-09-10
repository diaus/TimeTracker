package com.andrew.timetracker.database;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by andrew on 09.09.2016.
 */
public class dbHelper {
	public static List<Task> getRecentTasks(TimelineDao timelineDao, TaskDao taskDao, long excludeTaskId, int maxCount){

		List<Timeline> tt = timelineDao.queryBuilder()
				  .where(TimelineDao.Properties.TaskId.notEq(excludeTaskId))
				  .limit(100)
				  .orderDesc(TimelineDao.Properties.StartTime).list();

		List<Task> tasks = new ArrayList<>();
		Map<Long, Object> mapRecents = new HashMap<>();
		int count = 0;
		for (int i = 0; i < tt.size(); i++) {
			Long taskId = tt.get(i).getTaskId();
			if (!mapRecents.containsKey(taskId)) {
				mapRecents.put(taskId, null);
				tasks.add(0, taskDao.load(taskId));
				count++;
				if (count == maxCount) break;
			}
		}

		return tasks;
	}

	private void deleteInvalidTimelines(TimelineDao timelineDao) {
		// delete timelines with invalid taskId
		timelineDao.queryBuilder().where(new WhereCondition.StringCondition(TimelineDao.Properties.TaskId.columnName + " NOT IN (SELECT "
				  + TaskDao.Properties.Id.columnName + " FROM " + TaskDao.TABLENAME + ")")).buildDelete().executeDeleteWithoutDetachingEntities();

	}
}
