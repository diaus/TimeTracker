package com.andrew.timetracker.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.ISimpleCallback;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;

/**
 * Created by andrew on 10.09.2016.
 */
public class actionsHelper {

	public static void deleteTimeline(Context context, final TimelineDao timelineDao, Long timelineId, final ISimpleCallback onDeleteCallback) {
		final Timeline timeline = timelineDao.loadDeep(timelineId);
		new AlertDialog.Builder(context)
				  .setMessage(context.getResources().getString(R.string.confirm_delete_timeline)
							 + "\n" + helper.formatTimelinePeriod(timeline, true, context)
							 + " [ " + helper.formatSpentTime(context, timeline.getSpentSeconds(), true) + " ]"
							 + "\n" + timeline.getTask().getName())
				  .setTitle(R.string.confirm_dialog_title)
				  .setIcon(R.drawable.icon_alert)
				  .setNegativeButton(android.R.string.cancel, null)
				  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  timelineDao.delete(timeline);
						  if (onDeleteCallback != null){
							  onDeleteCallback.onActionComplete();
						  }
					  }
				  })
				  .show();
	}
}
