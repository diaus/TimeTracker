package com.andrew.timetracker.views.tasks;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.SimpleListView;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.database.dbHelper;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.MainActivityTabFragment;

import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 15.08.2016.
 */
public class TasksFragment extends MainActivityTabFragment {
	private static final String TAG = "tt: TasksFragment";

	private static final String SAVED_CURRENT_TASK_ID = "current_task_id";

	private static final int REQUEST_EDIT_TASK = 1;
	private static final String DIALOG_EDIT_TASK = "DialogEditTask";

	private Long mCurrentTaskId;
	private Task mCurrentTask;
	private Long startedTaskId;

	SimpleListView mTasksList;
	View mCurrentTaskView;
	TextView mCurrentTaskTitle;
	TextView mSubtasksTitle;
	Button btnCreateTask;
	Button btnStartStop;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tasks, container, false);
		Log.d(TAG, "onCreateView");

		if (savedInstanceState != null){
			mCurrentTaskId = savedInstanceState.getLong(SAVED_CURRENT_TASK_ID, -1);
			if (mCurrentTaskId == -1) mCurrentTaskId = null;
		}

		mCurrentTaskView = v.findViewById(R.id.fragment_tasks_task_container);
		mTasksList = (SimpleListView) v.findViewById(R.id.fragment_tasks_list);
		mCurrentTaskTitle = (TextView) v.findViewById(R.id.fragment_tasks_task_title);
		mSubtasksTitle = (TextView) v.findViewById(R.id.subtasks_title);
		btnCreateTask = (Button) v.findViewById(R.id.fragment_tasks_create_task_button);
		btnStartStop = (Button) v.findViewById(R.id.fragment_tasks_task_start_button);

		v.findViewById(R.id.fragment_tasks_up).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentTask(mCurrentTask.getParentId());
			}
		});

		v.findViewById(R.id.fragment_tasks_item_edit_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editCurrentTask();
			}
		});

		btnCreateTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				createTask();
			}
		});

		btnStartStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startStopCurrentTask();
			}
		});

		updateUI();

		return v;
	}

	private void setSubtitle(String subtitle){
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.getSupportActionBar().setSubtitle(subtitle);
	}

	private void updateUI() {
		startedTaskId = dbHelper.getStartedTaskId(timelineDao());

		setSubtitle(null);
		mCurrentTaskView.setVisibility(mCurrentTaskId != null ? View.VISIBLE : View.GONE);
		if (mCurrentTaskId != null){
			mCurrentTask = taskDao().load(mCurrentTaskId);
			mCurrentTaskTitle.setText(mCurrentTask.getName());
			if (mCurrentTask.getParentId() != null){
				setSubtitle(taskDao().load(mCurrentTask.getParentId()).getName());
			}
			boolean isStarted = mCurrentTaskId.equals(startedTaskId);
			btnStartStop.setText(isStarted ? R.string.button_stop : R.string.button_start);
			btnStartStop.setBackgroundResource(isStarted ? R.drawable.stopped_circle : R.drawable.started_circle);
		} else {
			mCurrentTask = null;
		}

		List<Task> tasks = dbHelper.getTasks(taskDao(), mCurrentTaskId);
		mTasksList.setAdapter(new TasksAdapter(tasks), R.layout.fragment_tasks_item);

		mSubtasksTitle.setVisibility(mCurrentTaskId != null && tasks.size() > 0 ? View.VISIBLE : View.GONE);
		btnCreateTask.setText(mCurrentTaskId != null ? R.string.create_subtask_button : R.string.create_task_button);

		getActivity().invalidateOptionsMenu();
	}

	private void createTask() {
		FragmentManager manager = getFragmentManager();
		TaskEditDialogFragment dialog = TaskEditDialogFragment.newInstance(null, mCurrentTaskId);
		dialog.setTargetFragment(this, REQUEST_EDIT_TASK);
		dialog.show(manager, DIALOG_EDIT_TASK);
	}

	private void editCurrentTask() {
		FragmentManager manager = getFragmentManager();
		TaskEditDialogFragment dialog = TaskEditDialogFragment.newInstance(mCurrentTaskId, null);
		dialog.setTargetFragment(this, REQUEST_EDIT_TASK);
		dialog.show(manager, DIALOG_EDIT_TASK);
	}

	private void deleteCurrentTask() {
		boolean isSubtasks = taskDao().queryBuilder()
				  .where(TaskDao.Properties.ParentId.isNotNull())
				  .where(TaskDao.Properties.ParentId.eq(mCurrentTaskId))
				  .limit(1).unique() != null;
		if (isSubtasks) {
			helper.alert(getContext(), R.string.alert_delete_task_with_subtasks_disallowed);
			return;
		}

		final boolean isTimelines = timelineDao().queryBuilder().where(TimelineDao.Properties.TaskId.eq(mCurrentTaskId)).limit(1).unique() != null;

		new AlertDialog.Builder(getContext())
				  .setMessage(isTimelines ? R.string.confirm_delete_task_with_timelines : R.string.confirm_delete_task)
				  .setTitle(R.string.confirm_dialog_title)
				  .setIcon(R.drawable.icon_alert)
				  .setNegativeButton(android.R.string.cancel, null)
				  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  if (isTimelines){
							  timelineDao().queryBuilder().where(TimelineDao.Properties.TaskId.eq(mCurrentTaskId)).buildDelete().executeDeleteWithoutDetachingEntities();
						  }
						  taskDao().delete(mCurrentTask);
						  postDbChange();
						  Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.toast_task_deleted_params_name), mCurrentTask.getName()), Toast.LENGTH_SHORT).show();
						  mCurrentTaskId = mCurrentTask.getParentId();
						  updateUI();
					  }
				  })
				  .show();
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SAVED_CURRENT_TASK_ID, mCurrentTaskId == null ? -1 : mCurrentTaskId);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "create menu");
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.tab_tasks, menu);

		MenuItem deleteItem = menu.findItem(R.id.menu_item_delete_task);

		deleteItem.setVisible(mCurrentTaskId != null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_delete_task:
				deleteCurrentTask();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void setCurrentTask(Long taskId) {
		mCurrentTaskId = taskId;
		updateUI();
	}

	void startStopCurrentTask() {
		boolean doStop = mCurrentTaskId.equals(startedTaskId);
		if (doStop){
			dbHelper.stopCurrentTask(timelineDao());
		} else {
			dbHelper.startTask(timelineDao(), mCurrentTaskId);
		}
		updateUI();
		postDbChange();
		if (!doStop){
			getActivityMain().switchToHomeTab();
		}
	}

	@Override
	public boolean doBack() {
		if (mCurrentTaskId == null) return false;
		setCurrentTask(mCurrentTask.getParentId());
		return true;
	}

	@Override
	public void onTabSelected() {
		// always refresh
		updateUI();
	}

	private class TaskHolder implements View.OnClickListener {

		Task mTask;
		TextView mTitleTextView;

		public TaskHolder(View itemView) {
			itemView.setOnClickListener(this);
			mTitleTextView = (TextView) itemView.findViewById(R.id.fragment_tasks_item_title);
		}

		public void bindTask(Task task){
			mTask = task;
			mTitleTextView.setText(task.getName());
			mTitleTextView.setTextColor(ContextCompat.getColor(getContext(),
					  task.getId().equals(startedTaskId) ? R.color.colorTaskTitleStartedInList : R.color.colorBlack));
		}

		@Override
		public void onClick(View v) {
			setCurrentTask(mTask.getId());
		}
	}

	private class TasksAdapter implements SimpleListView.IAdapter<TaskHolder> {

		List<Task> tasks;

		public TasksAdapter(List<Task> tasks) {
			this.tasks = tasks;
		}

		@Override
		public int getItemCount() {
			return tasks.size();
		}

		@Override
		public void bindViewHolder(TaskHolder taskHolder, int position) {
			taskHolder.bindTask(tasks.get(position));
		}

		@Override
		public TaskHolder createViewHolder(View v) {
			return new TaskHolder(v);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_EDIT_TASK && resultCode == Activity.RESULT_OK){
			TaskEditDialogFragment.Result result = TaskEditDialogFragment.getResult(data);
			mCurrentTaskId = result.taskId;
			updateUI();
		}
	}
}
