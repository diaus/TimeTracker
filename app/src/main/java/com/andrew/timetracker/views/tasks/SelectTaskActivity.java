package com.andrew.timetracker.views.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.andrew.timetracker.App;
import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.SimpleListView;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;

import java.util.List;

/**
 * Created by andrew on 05.09.2016.
 */
public class SelectTaskActivity extends AppCompatActivity {

	private static final String EXTRA_PARENT_TASK_ID = "parent_task_id";
	private static final String EXTRA_EXCLUDE_TASK_ID = "exclude_task_id";

	public static Intent newIntent(Context context, Long parentTaskId, Long excludeTaskId) {
		Intent intent = new Intent(context, SelectTaskActivity.class);
		intent.putExtra(EXTRA_PARENT_TASK_ID, parentTaskId == null ? -1 : parentTaskId);
		intent.putExtra(EXTRA_EXCLUDE_TASK_ID, excludeTaskId == null ? -1 : excludeTaskId);
		return intent;
	}

	Long mParentTaskId;
	Long mExcludeTaskId;

	private TaskDao taskDao;
	SimpleListView mTasksList;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_task);

		// restore values
		mExcludeTaskId = getIntent().getLongExtra(EXTRA_EXCLUDE_TASK_ID, -1);
		if (savedInstanceState != null){
			mParentTaskId = savedInstanceState.getLong(EXTRA_PARENT_TASK_ID, -1);
		} else {
			mParentTaskId = getIntent().getLongExtra(EXTRA_PARENT_TASK_ID, -1);
		}
		if (mExcludeTaskId == -1) mExcludeTaskId = null;
		if (mParentTaskId == -1) mParentTaskId = null;

		// DATABASE
		DaoSession daoSession = ((App) getApplication()).getDaoSession();
		taskDao = daoSession.getTaskDao();

		mTasksList = (SimpleListView) findViewById(R.id.activity_select_task_list);

		updateUI();
	}

	private void updateUI() {
		List<Task> tasks;
		if (mParentTaskId == null){
			tasks = taskDao.queryBuilder()
					  .where(TaskDao.Properties.ParentId.isNull())
					  .list();
			getSupportActionBar().setTitle(R.string.task_edit_dialog_parent_task_top);
		} else {
			tasks = taskDao.queryBuilder()
					  .where(TaskDao.Properties.ParentId.isNotNull())
					  .where(TaskDao.Properties.ParentId.eq(mParentTaskId))
					  .list();
			Task task = taskDao.load(mParentTaskId);
			getSupportActionBar().setTitle(task.getName());
		}

		mTasksList.setAdapter(new TasksAdapter(tasks), R.layout.fragment_tasks_item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_PARENT_TASK_ID, mParentTaskId == null ? -1 : mParentTaskId);
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
			if (task == null){
				mTitleTextView.setText(R.string.select_task_up);
			} else {
				mTitleTextView.setText(task.getName());
			}
		}

		@Override
		public void onClick(View v) {
			if (mTask != null){
				if (mExcludeTaskId != null && mExcludeTaskId.equals(mTask.getId())){
					// don't allow select parent of self
					finishWithTask(mParentTaskId);
				} else {
					mParentTaskId = mTask.getId();
					boolean isLeaf = taskDao.queryBuilder()
							  .where(TaskDao.Properties.ParentId.isNotNull())
							  .where(TaskDao.Properties.ParentId.eq(mParentTaskId))
							  .limit(1).unique() == null;
					if (isLeaf){
						finishWithTask(mTask.getId());
					} else {
						updateUI();
					}
				}
			} else {
				Task task = taskDao.load(mParentTaskId);
				mParentTaskId = task.getParentId();
				updateUI();
			}
		}
	}

	private void finishWithTask(Long taskId) {
		Intent data = new Intent();
		data.putExtra(EXTRA_PARENT_TASK_ID, taskId == null ? -1 : taskId);
		setResult(Activity.RESULT_OK, data);
		finish();
	}

	public static Long getSelectedTaskId(Intent data){
		long taskId = data.getLongExtra(EXTRA_PARENT_TASK_ID, -1);
		return taskId == -1 ? null : taskId;
	}

	private class TasksAdapter implements SimpleListView.IAdapter<TaskHolder> {

		List<Task> tasks;

		public TasksAdapter(List<Task> tasks) {
			this.tasks = tasks;
		}

		@Override
		public int getItemCount() {
			int count = tasks.size();
			if (mParentTaskId != null) count += 1;
			return count;
		}

		@Override
		public void bindViewHolder(TaskHolder taskHolder, int position) {
			if (mParentTaskId != null) position -= 1;
			taskHolder.bindTask(position < 0 ? null : tasks.get(position));
		}

		@Override
		public TaskHolder createViewHolder(View v) {
			return new TaskHolder(v);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.select_task, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_select:
				finishWithTask(mParentTaskId);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
