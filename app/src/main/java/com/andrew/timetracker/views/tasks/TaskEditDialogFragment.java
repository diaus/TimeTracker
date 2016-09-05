package com.andrew.timetracker.views.tasks;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.andrew.timetracker.App;
import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.SimpleTextWatcher;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;

import java.io.Serializable;

/**
 * Created by andrew on 05.09.2016.
 */
public class TaskEditDialogFragment extends DialogFragment {
	private static final String ARG_TASK_ID = "id";
	private static final String ARG_PARENT_ID = "parent_id";

	private static final String SAVED_PARENT_ID = "parent_id";
	private static final int REQUEST_SELECT_TASK = 1;

	public class Result implements Serializable {
		public Long taskId;

		public Result(Long taskId) {
			this.taskId = taskId;
		}
	}

	Long mTaskId;
	Long mParentTaskId;
	TaskDao mTaskDao = null;

	EditText mTaskName;
	TextView mParentTask;

	Button mSaveButton;
	private int mSaveButtonColor;

	public static TaskEditDialogFragment newInstance(Long taskId, Long parentTaskId) {
		Bundle args = new Bundle();
		args.putLong(ARG_TASK_ID, taskId != null ? taskId : -1);
		args.putLong(ARG_PARENT_ID, parentTaskId != null ? parentTaskId : -1);
		TaskEditDialogFragment fragment = new TaskEditDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mTaskId = getArguments().getLong(ARG_TASK_ID);
		if (mTaskId == -1) mTaskId = null;
		boolean isCreate = mTaskId == null;
		mParentTaskId = getArguments().getLong(ARG_PARENT_ID);
		if (mParentTaskId == -1) mParentTaskId = null;

		DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
		mTaskDao = daoSession.getTaskDao();

		View v = LayoutInflater.from(getContext()).inflate(R.layout.task_edit_dialog, null);
		mTaskName = (EditText) v.findViewById(R.id.task_edit_dialog_task_name);
		mParentTask = (TextView) v.findViewById(R.id.task_edit_dialog_parent_task);

		mTaskName.addTextChangedListener(new SimpleTextWatcher() {
			@Override
			protected void onChange(String text) {
				validate();
			}
		});

		if (savedInstanceState != null){
			mParentTaskId = savedInstanceState.getLong(SAVED_PARENT_ID);
			if (mParentTaskId == -1) mParentTaskId = null;
		} else {
			// first load
			if (!isCreate){
				Task task = mTaskDao.load(mTaskId);
				mTaskName.setText(task.getName());
				mParentTaskId = task.getParentId();
			}
		}

		updateParentTaskUI();

		v.findViewById(R.id.task_edit_dialog_parent_task_edit_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Long parentId = mParentTaskId;
				if (parentId != null) parentId = mTaskDao.load(parentId).getParentId();
				startActivityForResult(SelectTaskActivity.newIntent(getContext(), parentId, mTaskId), REQUEST_SELECT_TASK);
			}
		});

		AlertDialog alert = new AlertDialog.Builder(getActivity())
				  .setView(v)
				  .setNegativeButton(android.R.string.cancel, null)
				  .setPositiveButton(isCreate ? R.string.dialog_create_button : R.string.dialog_save_button, null)
				  .create();

		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				mSaveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				mSaveButtonColor = mSaveButton.getCurrentTextColor();
				mSaveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveTask(dialog);
					}
				});
				validate();
			}
		});

		return alert;
	}

	private void updateParentTaskUI() {
		if (mParentTaskId != null){
			Task task = mTaskDao.load(mParentTaskId);
			mParentTask.setText(task.getName());
		} else {
			mParentTask.setText(R.string.task_edit_dialog_parent_task_top);
		}
	}

	private void validate() {
		if (mSaveButton == null) return;
		boolean isValid = !mTaskName.getText().toString().isEmpty();
		mSaveButton.setEnabled(isValid);
		mSaveButton.setTextColor(isValid ? mSaveButtonColor : Color.GRAY);
	}

	private void saveTask(DialogInterface dialog){
		boolean isCreate = mTaskId == null;
		Task task;
		if (isCreate){
			task = new Task(null, mTaskName.getText().toString(), mParentTaskId);
			mTaskDao.insert(task);
		} else {
			task = mTaskDao.load(mTaskId);
			task.setName(mTaskName.getText().toString());
			task.setParentId(mParentTaskId);
			mTaskDao.update(task);
		}

		Intent result = new Intent();
		result.putExtra("result", new Result(task.getId()));
		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, result);
		dialog.dismiss();
	}

	public static Result getResult(Intent data){
		return (Result) data.getSerializableExtra("result");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SELECT_TASK && resultCode == Activity.RESULT_OK){
			mParentTaskId = SelectTaskActivity.getSelectedTaskId(data);
			updateParentTaskUI();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SAVED_PARENT_ID, mParentTaskId == null ? -1 : mParentTaskId);
	}
}
