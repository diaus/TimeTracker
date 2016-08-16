package com.andrew.timetracker;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tab_tasks, container, false);
		Log.d(TAG, "onCreateView");

		// DATABASE
		DaoSession daoSession = ((App)getActivity().getApplication()).getDaoSession();
		taskDao = daoSession.getTaskDao();
		tasksQuery = taskDao.queryBuilder().orderAsc(TaskDao.Properties.Name).build();

		mPanelAdd = v.findViewById(R.id.fragment_tab_tasks_list_panel_add);
		mPanelAddText = (EditText) v.findViewById(R.id.fragment_tab_tasks_list_panel_add_text);
		mPanelAddDoneButton = (ImageButton) v.findViewById(R.id.fragment_tab_tasks_list_panel_add_done);
		mPanelAddDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Task task = new Task(null, mPanelAddText.getText().toString());
				taskDao.insert(task);
				mPanelAddText.setText("");
				mPanelAdd.setVisibility(View.GONE);
				updateTasks();
				//Toast.makeText(TabTasksFragment.this.getActivity(), "asd", Toast.LENGTH_SHORT).show();
			}
		});

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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_new_task:
				if (mPanelAdd.getVisibility() == View.VISIBLE){
					mPanelAdd.setVisibility(View.GONE);
				} else{
					mPanelAdd.setVisibility(View.VISIBLE);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateTasks() {
		List<Task> tasks = tasksQuery.list();
		mAdapter.setTasks(tasks);
	}

	private class TaskHolder extends RecyclerView.ViewHolder{

		TextView mTitleTextView;

		public TaskHolder(View itemView) {
			super(itemView);
			mTitleTextView = (TextView) itemView.findViewById(R.id.tab_tasks_item_title);
		}

		public void bindTask(Task task){
			mTitleTextView.setText(task.getName());
		}

	}

	private class TasksAdapter extends RecyclerView.Adapter<TaskHolder>{

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
			return new TaskHolder(view);		}

		@Override
		public void onBindViewHolder(TaskHolder holder, int position) {
			Task task = mTasks.get(position);
			holder.bindTask(task);
		}

		@Override
		public int getItemCount() {
			return mTasks.size();
		}
	}

}
