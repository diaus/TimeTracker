package com.andrew.timetracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andrew.timetracker.commons.SimpleTextWatcher;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;

import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 15.08.2016.
 */
public class TabTasksFragment extends Fragment {

	private static final String TAG = "tt: TabTasksFragment";

	private TaskDao taskDao;
	private Query<Task> tasksQuery;

	RecyclerView mTasksRecyclerView;
	TasksAdapter mAdapter;
	View mPanelAdd;
	EditText mPanelAddText;
	ImageButton mPanelAddDoneButton;

	int mSelectedTaskPosition;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tab_tasks, container, false);
		Log.d(TAG, "onCreateView");

		// DATABASE
		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		taskDao = daoSession.getTaskDao();
		tasksQuery = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Name).build();

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
				Task task = new Task(null, mPanelAddText.getText().toString());
				taskDao.insert(task);
				mPanelAddText.setText("");
				mPanelAdd.setVisibility(View.GONE);
				getActivity().invalidateOptionsMenu();
				updateTasks();
				mAdapter.selectTaskById(task.getId());
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
				mPanelAdd.setVisibility(View.VISIBLE);
				getActivity().invalidateOptionsMenu();
				mPanelAddText.requestFocus();
				return true;
			case R.id.menu_item_cancel:
				mPanelAddText.setText(null);
				mPanelAdd.setVisibility(View.GONE);
				getActivity().invalidateOptionsMenu();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateTasks() {
		mSelectedTaskPosition = -1;
		List<Task> tasks = tasksQuery.list();
		mAdapter.setTasks(tasks);
	}

	private void onTaskSelected(int position){
		int prevPos = mSelectedTaskPosition;
		mSelectedTaskPosition = position;
		if (prevPos != -1){
			mAdapter.notifyItemChanged(prevPos);
		}
		if (mPanelAdd.getVisibility() == View.VISIBLE){
			mPanelAddText.setText(mAdapter.getItem(position).getName());
			mPanelAddText.requestFocus();
			mPanelAddText.setSelection(mPanelAddText.getText().length());
		}
	}

	private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

		View mContainer;
		TextView mTitleTextView;
		View mDivider;
		boolean mSelected;

		public TaskHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this);
			mContainer = itemView.findViewById(R.id.tab_tasks_item_container);
			mTitleTextView = (TextView) itemView.findViewById(R.id.tab_tasks_item_title);
			mDivider = itemView.findViewById(R.id.tab_tasks_item_divider);
		}

		public void bindTask(Task task, boolean isLast, boolean isSelected) {
			mSelected = isSelected;
			mTitleTextView.setText(task.getName());
			mDivider.setVisibility(isLast ? View.INVISIBLE : View.VISIBLE);
			updateSelected();
		}

		private void updateSelected(){
			mContainer.setBackgroundResource(mSelected ? R.drawable.task_selected_bg : 0);
		}

		@Override
		public void onClick(View v) {
			if (mSelected) return;
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

		public Task getItem(int position) {
			return mTasks.get(position);
		}

		public void selectTaskById(long id){
			int prevPos = mSelectedTaskPosition;
			mSelectedTaskPosition = -1;
			if (prevPos != -1){
				notifyItemChanged(prevPos);
			}
			for (int i = 0; i < mTasks.size(); i++) {
				if (mTasks.get(i).getId() == id){
					mSelectedTaskPosition = i;
					notifyItemChanged(mSelectedTaskPosition);
					mTasksRecyclerView.scrollToPosition(mSelectedTaskPosition);
					break;
				}
			}
		}

	}

}
