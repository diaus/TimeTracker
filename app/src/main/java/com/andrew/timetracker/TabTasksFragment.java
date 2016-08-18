package com.andrew.timetracker;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andrew.timetracker.commons.OnDoubleTouchListener;
import com.andrew.timetracker.commons.SimpleTextWatcher;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.settings.IMainActivity;

import org.greenrobot.greendao.query.Query;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by andrew on 15.08.2016.
 */
public class TabTasksFragment extends Fragment implements MainActivity.ITab {

	private static final String TAG = "tt: TabTasksFragment";

	private TaskDao taskDao;
	private TimelineDao timelineDao;
	private Query<Task> tasksQuery;

	RecyclerView mTasksRecyclerView;
	TasksAdapter mAdapter;
	View mPanelAdd;
	EditText mPanelAddText;
	ImageButton mPanelAddDoneButton;

	int mSelectedTaskPosition = -1;
	long mStartedTaskId = -1;
	boolean mIsEditing = false;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tab_tasks, container, false);
		Log.d(TAG, "onCreateView");

		// DATABASE
		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		taskDao = daoSession.getTaskDao();
		tasksQuery = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Name).build();
		timelineDao = daoSession.getTimelineDao();

		mPanelAdd = v.findViewById(R.id.fragment_tab_tasks_list_panel_add);

		mPanelAddText = (EditText) v.findViewById(R.id.fragment_tab_tasks_list_panel_add_text);
		mPanelAddText.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			protected void onChange(String text) {
				mPanelAddDoneButton.setEnabled(text.length() > 0);
			}
		});

		mPanelAddDoneButton = (ImageButton) v.findViewById(R.id.fragment_tab_tasks_list_panel_add_done);
		mPanelAddDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				String newName = mPanelAddText.getText().toString();

				// check unique name
				Task exTask = taskDao.queryBuilder().where(TaskDao.Properties.Name.eq(newName)).unique();
				boolean isDuplicated = false;

				Task task;
				long taskId = -1;
				if (mIsEditing) {
					task = mAdapter.getTask(mSelectedTaskPosition);
					taskId = task.getId();
					if (exTask != null){
						isDuplicated = exTask.getId().equals(task.getId());
					}
					if (!isDuplicated){
						task.setName(newName);
						taskDao.update(task);
						mIsEditing = false;
					}
				} else {
					isDuplicated = exTask != null;
					if (!isDuplicated){
						task = new Task(null, newName);
						taskDao.insert(task);
						taskId = task.getId();
					}
				}
				if (isDuplicated){
					new AlertDialog.Builder(getContext())
							  .setMessage(R.string.alert_task_name_exists)
							  .setTitle(R.string.alert_task_name_exists_title)
							  .setIcon(R.drawable.icon_alert)
							  .setPositiveButton(android.R.string.ok, null)
							  .show();

				} else {
					setPanelAddVisibility(false);
					updateTasks();
					mAdapter.selectTaskById(taskId);
				}
			}
		});
		mPanelAddDoneButton.setEnabled(false);

		mTasksRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_tab_tasks_list_recycler_view);
		mTasksRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		mAdapter = new TasksAdapter();
		mTasksRecyclerView.setAdapter(mAdapter);

		updateTasks();

		return v;
	}

	private IMainActivity getHostingActivity(){
		return (IMainActivity) getActivity();
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

		MenuItem createItem = menu.findItem(R.id.menu_item_new_task);
		MenuItem cancelItem = menu.findItem(R.id.menu_item_cancel);

		boolean isAddPanel = mPanelAdd.getVisibility() == View.VISIBLE;
		createItem.setVisible(!isAddPanel);
		cancelItem.setVisible(isAddPanel);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_new_task:
				setPanelAddVisibility(true);
				if (mSelectedTaskPosition != -1) {
					int pos = mSelectedTaskPosition;
					mSelectedTaskPosition = -1;
					mAdapter.notifyItemChanged(pos);
				}
				return true;
			case R.id.menu_item_cancel:
				setPanelAddVisibility(false);
				mIsEditing = false;
				if (mSelectedTaskPosition != -1) {
					mAdapter.notifyItemChanged(mSelectedTaskPosition);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateTasks() {
		updateStartedTask();
		mSelectedTaskPosition = -1;
		mIsEditing = false;
		List<Task> tasks = tasksQuery.list();
		mAdapter.setTasks(tasks);
	}

	private void updateStartedTask() {
		Timeline timeline = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		long taskId = timeline == null ? -1 : timeline.getTaskId();
		if (taskId != mStartedTaskId){
			long prevTaskId = mStartedTaskId;
			mStartedTaskId = taskId;
			mAdapter.updateTaskById(prevTaskId);
			mAdapter.updateTaskById(taskId);
		}
	}

	private void onTaskSelected(int position) {
		int prevPos = mSelectedTaskPosition;
		mSelectedTaskPosition = position;
		if (prevPos != -1) {
			mAdapter.notifyItemChanged(prevPos);
		}
		if (mPanelAdd.getVisibility() == View.VISIBLE) {
			setPanelAddVisibility(true, mAdapter.getTask(position).getName());
		}
	}

	void setPanelAddVisibility(boolean isVisible, String text) {
		mPanelAddText.setText(text);
		mPanelAdd.setVisibility(isVisible ? View.VISIBLE : View.GONE);
		final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (isVisible) {
			mPanelAddText.requestFocus();
			mPanelAddText.setSelection(mPanelAddText.getText().length());
			imm.showSoftInput(mPanelAddText, InputMethodManager.SHOW_IMPLICIT);
		} else {
			imm.hideSoftInputFromWindow(mPanelAddText.getWindowToken(), 0);
		}
		getActivity().invalidateOptionsMenu();
	}

	void setPanelAddVisibility(boolean isVisible) {
		setPanelAddVisibility(isVisible, "");
	}

	private void deleteSelectedTask() {
		Task task = mAdapter.getTask(mSelectedTaskPosition);
		boolean isTimelines = timelineDao.queryBuilder().where(TimelineDao.Properties.TaskId.eq(task.getId())).limit(1).unique() != null;

		new AlertDialog.Builder(getContext())
				  .setMessage(isTimelines ? R.string.confirm_delete_task_with_timelines : R.string.confirm_delete_task)
				  .setTitle(R.string.confirm_delete_task_title)
				  .setIcon(R.drawable.icon_alert)
				  .setNegativeButton(android.R.string.cancel, null)
				  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					  @Override
					  public void onClick(DialogInterface dialog, int which) {
						  Task task = mAdapter.getTask(mSelectedTaskPosition);
						  timelineDao.queryBuilder().where(TimelineDao.Properties.TaskId.eq(task.getId())).buildDelete().executeDeleteWithoutDetachingEntities();
						  taskDao.delete(task);
						  updateTasks();
						  Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.toast_task_deleted_params_name), task.getName()), Toast.LENGTH_SHORT).show();
					  }
				  })
				  .show();
	}

	void stopCurrentTask() {
		long prevTaskId = mStartedTaskId;
		Timeline timeline = timelineDao.queryBuilder().where(TimelineDao.Properties.StopTime.isNull()).unique();
		if (timeline == null) return;
		timeline.setStopTime(new Date());
		timeline.update();
		mStartedTaskId = -1;
		if (prevTaskId != -1){
			mAdapter.updateTaskById(timeline.getTaskId());
		}
	}

	void startSelectedTask() {
		stopCurrentTask();
		long taskId = mAdapter.getTask(mSelectedTaskPosition).getId();
		Timeline timeline = new Timeline(null, taskId, new Date(), null);
		timelineDao.insert(timeline);
		mStartedTaskId = taskId;
		mAdapter.updateTaskById(taskId);
		getHostingActivity().switchToHomeTab();
	}

	@Override
	public void onTabSelected() {
		updateStartedTask();
	}

	private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		View mContainer;
		TextView mTitleTextView;
		View mDivider;
		ImageButton mStartButton;
		ImageButton mEditButton;
		ImageButton mDeleteButton;
		boolean mSelected;
		boolean mIsStarted;

		public TaskHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			mContainer = itemView.findViewById(R.id.tab_tasks_item_container);
			mTitleTextView = (TextView) itemView.findViewById(R.id.tab_tasks_item_title);
			mDivider = itemView.findViewById(R.id.tab_tasks_item_divider);
			mEditButton = (ImageButton) itemView.findViewById(R.id.tab_tasks_item_edit_button);
			mDeleteButton = (ImageButton) itemView.findViewById(R.id.tab_tasks_item_delete_button);
			mStartButton = (ImageButton) itemView.findViewById(R.id.tab_tasks_item_start_button);

			mStartButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startSelectedTask();
				}
			});

			mEditButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsEditing = true;
					setPanelAddVisibility(true, mTitleTextView.getText().toString());
					updateSelected();
				}
			});

			mDeleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteSelectedTask();
				}
			});
		}

		public void bindTask(Task task, boolean isLast, boolean isSelected) {
			mSelected = isSelected;
			mIsStarted = task.getId().equals(mStartedTaskId);
			mTitleTextView.setText(task.getName());

			mTitleTextView.setTextColor(ContextCompat.getColor(getContext(),
					  mIsStarted ? R.color.colorTaskTitleStartedInList : android.R.color.primary_text_light));
			mDivider.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
			updateSelected();
		}

		private void updateSelected() {
			mContainer.setBackgroundResource(mSelected ? R.drawable.task_selected_bg : 0);
			boolean showButtons = mSelected && mPanelAdd.getVisibility() != View.VISIBLE;
			mEditButton.setVisibility(showButtons ? View.VISIBLE : View.GONE);
			mDeleteButton.setVisibility(showButtons ? View.VISIBLE : View.GONE);
			boolean showStartButton = showButtons && !mIsStarted;
			int visibilityStartButton = showStartButton ? View.VISIBLE : View.GONE;
			if (mStartButton.getVisibility() != visibilityStartButton){
				mStartButton.setVisibility(visibilityStartButton);
				mContainer.setOnTouchListener(!showStartButton ? null : new OnDoubleTouchListener(getContext()) {
					@Override
					protected boolean onDoubleTab() {
						startSelectedTask();
						return true;
					}
				});
			}
		}

		@Override
		public void onClick(View v) {
			if (mSelected || mIsEditing) return;
			mSelected = true;
			updateSelected();
			onTaskSelected(getAdapterPosition());
		}
	}

	private class TasksAdapter extends RecyclerView.Adapter<TaskHolder> {

		private List<Task> mTasks;

		public TasksAdapter() {
			mTasks = new ArrayList<>();
		}

		public void setTasks(@NonNull List<Task> tasks) {
			mTasks = tasks;
			notifyDataSetChanged();
		}

		@Override
		public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			View view = layoutInflater.inflate(R.layout.tab_tasks_item, parent, false);
			return new TaskHolder(view);
		}

		@Override
		public void onBindViewHolder(TaskHolder holder, int position) {
			Task task = mTasks.get(position);
			holder.bindTask(task, position == getItemCount() - 1, position == mSelectedTaskPosition);
		}

		@Override
		public int getItemCount() {
			return mTasks.size();
		}

		public Task getTask(int position) {
			return mTasks.get(position);
		}

		public void updateTaskById(long id){
			if (id == -1) return;
			for (int i = 0; i < mTasks.size(); i++) {
				if (mTasks.get(i).getId() == id) {
					notifyItemChanged(i);
					break;
				}
			}
		}

		public void selectTaskById(long id) {
			int prevPos = mSelectedTaskPosition;
			mSelectedTaskPosition = -1;
			if (prevPos != -1) {
				notifyItemChanged(prevPos);
			}
			for (int i = 0; i < mTasks.size(); i++) {
				if (mTasks.get(i).getId() == id) {
					mSelectedTaskPosition = i;
					notifyItemChanged(i);
					mTasksRecyclerView.scrollToPosition(i);
					break;
				}
			}
		}

	}

}
