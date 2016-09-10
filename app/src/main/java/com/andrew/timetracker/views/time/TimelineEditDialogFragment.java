package com.andrew.timetracker.views.time;

import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.TextView;

import com.andrew.timetracker.App;
import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.TimePicker;
import com.andrew.timetracker.database.DaoSession;
import com.andrew.timetracker.database.Task;
import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.utils.helper;
import com.andrew.timetracker.views.tasks.SelectTaskActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by andrew on 23.08.2016.
 */
public class TimelineEditDialogFragment extends DialogFragment {

	private static final String ARG_TIMELINE_ID = "id";

	private static final String SAVED_DATE_TO = "date_to";
	private static final String SAVED_DATE_FROM = "date_from";
	private static final String SAVED_TASK_ID = "task_id";

	private static final int REQUEST_SELECT_TASK = 1;

	TimelineDao mTimelineDao = null;
	TaskDao mTaskDao = null;

	long mTimelineId;
	long mTaskId;

	Date mDateFromInitial, mDateToInitial;
	long mTaskIdInitial;
	boolean isStarted;
	Calendar mDateFrom, mDateTo;

	private int mSaveButtonColor;

	Button mSaveButton;
	Button mDateFromButton;
	Button mDateToButton;
	TimePicker mTimeFromPicker;
	TimePicker mTimeToPicker;
	TextView mTimeSpentTextView;
	Button btnTask;

	public static TimelineEditDialogFragment newInstance(long timelineId) {
		Bundle args = new Bundle();
		args.putLong(ARG_TIMELINE_ID, timelineId);
		TimelineEditDialogFragment fragment = new TimelineEditDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		ensureDao();
		mTimelineId = getArguments().getLong(ARG_TIMELINE_ID);
		Timeline timeline = mTimelineDao.load(mTimelineId);

		mTaskIdInitial = timeline.getTaskId();
		mDateFromInitial = timeline.getStartTime();
		mDateToInitial = timeline.getStopTime();
		isStarted = mDateToInitial == null;

		mDateFrom = Calendar.getInstance();
		mDateTo = mDateToInitial != null ? Calendar.getInstance() : null;

		if (savedInstanceState != null){
			// first call
			mTaskId = savedInstanceState.getLong(SAVED_TASK_ID);
			mDateFrom.setTime(new Date(savedInstanceState.getLong(SAVED_DATE_FROM)));
			if (!isStarted){
				mDateTo.setTime(new Date(savedInstanceState.getLong(SAVED_DATE_TO)));
			}
		} else {
			mTaskId = timeline.getTaskId();
			mDateFrom.setTime(mDateFromInitial);
			if (!isStarted){
				mDateTo.setTime(mDateToInitial);
			}
		}

		View v = LayoutInflater.from(getActivity()).inflate(R.layout.timeline_edit_dialog, null);

		btnTask = (Button) v.findViewById(R.id.timeline_edit_dialog_task);
		btnTask.setText(mTaskDao.load(mTaskId).getName());
		btnTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doSelectTask();
			}
		});

		mDateFromButton = (Button) v.findViewById(R.id.timeline_edit_dialog_date_from_button);
		mDateToButton = (Button) v.findViewById(R.id.timeline_edit_dialog_date_to_button);

		mDateFromButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickDate(true);
			}
		});
		mDateToButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickDate(false);
			}
		});

		mTimeFromPicker = (TimePicker) v.findViewById(R.id.timeline_edit_dialog_time_from);
		initTimePicker(mTimeFromPicker, mDateFrom.getTime());

		mTimeToPicker = (TimePicker) v.findViewById(R.id.timeline_edit_dialog_time_to);
		if (isStarted) {
			mDateToButton.setVisibility(View.GONE);
			mTimeToPicker.setVisibility(View.GONE);
			v.findViewById(R.id.timeline_edit_dialog_date_to_now).setVisibility(View.VISIBLE);
		} else {
			initTimePicker(mTimeToPicker, mDateTo.getTime());
		}

		mTimeSpentTextView = (TextView) v.findViewById(R.id.timeline_edit_dialog_time_spent);

		AlertDialog alert = new AlertDialog.Builder(getActivity())
				  .setView(v)
				  .setTitle(R.string.timeline_edit_dialog_title)
				  .setNegativeButton(android.R.string.cancel, null)
				  .setPositiveButton(R.string.dialog_save_button, null)
				  .create();

		alert.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(final DialogInterface dialog) {
				mSaveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
				mSaveButtonColor = mSaveButton.getCurrentTextColor();
				mSaveButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						saveTimeline(dialog);
					}
				});
				updateUI();
			}
		});

		return alert;
	}

	private void doSelectTask() {
		startActivityForResult(SelectTaskActivity.newIntent(getContext(), null, null), REQUEST_SELECT_TASK);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(SAVED_DATE_FROM, mDateFrom.getTime().getTime());
		if (mDateToInitial != null){
			outState.putLong(SAVED_DATE_TO, mDateTo.getTime().getTime());
		}
		outState.putLong(SAVED_TASK_ID, mTaskId);
	}

	private void initTimePicker(TimePicker timePicker, Date time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		timePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
		timePicker.setCurrentSecond(cal.get(Calendar.SECOND));
		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute, int seconds) {
				boolean isFrom = view == mTimeFromPicker;
				Calendar cal = isFrom ? mDateFrom : mDateTo;
				cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
				cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.SECOND, seconds);
				updateUI();
			}
		});
	}


	private void pickDate(final boolean isFrom) {
		Calendar cal = isFrom ? mDateFrom : mDateTo;
		new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				Calendar cal = isFrom ? mDateFrom : mDateTo;
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, monthOfYear);
				cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateUI();
			}
		}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
	}

	private void updateUI() {
		mDateFromButton.setText(String.format(Locale.getDefault(), "%1$tY %1$tb %1$td", mDateFrom));

		if (!isStarted) {
			mDateToButton.setText(String.format(Locale.getDefault(), "%1$tY %1$tb %1$td", mDateTo));
		}

		int spent = helper.diffDates(mDateFrom, mDateTo);
		String sSpent = helper.formatSpentTime(getContext(), Math.abs(spent), true);
		if (spent < 0) sSpent = "-" + sSpent;
		mTimeSpentTextView.setText(sSpent);

		validate();
	}

	private void validate() {
		if (mSaveButton == null) return;

		Calendar dateNow = Calendar.getInstance();
		Calendar dateTo = mDateToInitial == null ? dateNow : mDateTo;

		boolean isValid =
				  (mDateToInitial == null || mDateTo.before(dateNow))
				  && mDateFrom.before(dateTo)
				  && mDateFrom.get(Calendar.YEAR) == dateTo.get(Calendar.YEAR)
				  && mDateFrom.get(Calendar.MONTH) == dateTo.get(Calendar.MONTH)
				  && mDateFrom.get(Calendar.DAY_OF_MONTH) == dateTo.get(Calendar.DAY_OF_MONTH)
				  && (mTaskId != mTaskIdInitial || !mDateFrom.getTime().equals(mDateFromInitial) || (mDateToInitial != null && !mDateToInitial.equals(mDateTo.getTime())))
				  ;

		mSaveButton.setEnabled(isValid);
		mSaveButton.setTextColor(isValid ? mSaveButtonColor : Color.GRAY);
	}

	private void saveTimeline(DialogInterface dialog) {

		ensureDao();

		Date dateFrom = mDateFrom.getTime();
		Date dateTo = isStarted ? new Date() : mDateTo.getTime();

		// check intercepting with other timelines
		boolean isValid = mTimelineDao.queryBuilder().where(TimelineDao.Properties.Id.notEq(mTimelineId))
				  .where(TimelineDao.Properties.StartTime.lt(dateTo))
				  .where(TimelineDao.Properties.StopTime.isNull())
				  .limit(1).unique() == null;

		isValid = isValid && mTimelineDao.queryBuilder().where(TimelineDao.Properties.Id.notEq(mTimelineId))
				  .where(TimelineDao.Properties.StartTime.lt(dateFrom))
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.gt(dateFrom))
				  .limit(1).unique() == null;

		isValid = isValid && mTimelineDao.queryBuilder().where(TimelineDao.Properties.Id.notEq(mTimelineId))
				  .where(TimelineDao.Properties.StartTime.lt(dateTo))
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.gt(dateTo))
				  .limit(1).unique() == null;

		isValid = isValid && mTimelineDao.queryBuilder().where(TimelineDao.Properties.Id.notEq(mTimelineId))
				  .where(TimelineDao.Properties.StartTime.ge(dateFrom))
				  .where(TimelineDao.Properties.StopTime.isNotNull())
				  .where(TimelineDao.Properties.StopTime.le(dateTo))
				  .limit(1).unique() == null;

		if (!isValid){
			new AlertDialog.Builder(getContext())
					  .setMessage(R.string.timeline_edit_dialog_error_intercept_timelines)
					  .setTitle(R.string.error_dialog_title)
					  .setIcon(R.drawable.icon_alert)
					  .setPositiveButton(android.R.string.ok, null)
					  .show();

			return;
		}

		Timeline timeline = mTimelineDao.load(mTimelineId);
		timeline.setStartTime(dateFrom);
		if (!isStarted){
			timeline.setStopTime(dateTo);
		}
		timeline.setTaskId(mTaskId);
		mTimelineDao.update(timeline);
		helper.postDbChange(this);

		getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
		dialog.dismiss();
	}

	private void ensureDao() {
		if (mTimelineDao == null) {
			DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
			mTimelineDao = daoSession.getTimelineDao();
			mTaskDao = daoSession.getTaskDao();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_SELECT_TASK && resultCode == Activity.RESULT_OK){
			Long taskId = SelectTaskActivity.getSelectedTaskId(data);
			if (taskId != null){
				mTaskId = taskId;
				btnTask.setText(mTaskDao.load(mTaskId).getName());
				validate();
			}
		}
	}
}
