package com.andrew.timetracker.database;

import com.andrew.timetracker.utils.helper;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
				tasks.add(taskDao.load(taskId));
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

	public static List<Task> getTasks(TaskDao taskDao, Long parentTaskId) {
		QueryBuilder<Task> qb = taskDao.queryBuilder();
		if (parentTaskId != null) {
			qb.where(TaskDao.Properties.ParentId.isNotNull()).where(TaskDao.Properties.ParentId.eq(parentTaskId));
		} else {
			qb.where(TaskDao.Properties.ParentId.isNull());
		}
		List<Task> tasks = qb.list();
		Collections.sort(tasks, new Comparator<Task>() {
			@Override
			public int compare(Task t1, Task t2) {
				return helper.collator.compare(t1.getName(), t2.getName());
			}
		});
		return tasks;
	}

	public static Long getStartedTaskId(TimelineDao timelineDao) {
		Timeline tl = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		return tl == null ? null : tl.getTaskId();
	}

	public static boolean stopCurrentTask(TimelineDao timelineDao) {
		Timeline tl = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		if (tl == null) return false;
		tl.setStopTime(new Date());
		timelineDao.update(tl);
		return true;
	}

	public static Timeline startTask(TimelineDao timelineDao, Long taskId) {
		stopCurrentTask(timelineDao); // ensure stop previous task
		Timeline tl = new Timeline(null, taskId, new Date(), null);
		timelineDao.insert(tl);
		return tl;
	}
}
