package com.andrew.timetracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by andrew on 15.08.2016.
 */
public class TabHomeFragment extends Fragment {

	Date mDateStarted;
	int mSpentToday;
	boolean mStarted;

	Button mStartButton;
	Button mClearButton;
	View mStatusView;
	TextView mSpentTimeTextView;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_tab_home, container, false);

		mStartButton = (Button) v.findViewById(R.id.fragment_tab_home_start_button);
		mStatusView = (View) v.findViewById(R.id.fragment_tab_home_view_status);
		mSpentTimeTextView = (TextView) v.findViewById(R.id.fragment_tab_home_spent_time_textView);
		mClearButton = (Button) v.findViewById(R.id.fragment_tab_home_clear_button);

		mStartButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSpentToday += getStartedTaskTime();
				mDateStarted = new Date();
				mStarted = !mStarted;
				updateStatus();
				updateTime();
				saveData();
			}
		});

		mClearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSpentToday = 0;
				updateStatus();
				updateTime();
				saveData();
			}
		});

		loadData();

		updateStatus();
		updateTime();

		return v;
	}

	private int getStartedTaskTime() {
		if (!mStarted) return 0;
		return (int) ((System.currentTimeMillis() - mDateStarted.getTime())/1000);
	}

	private void loadData() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mSpentToday = pref.getInt("spent", 0);
		mStarted = pref.getBoolean("started", false);
		if (mStarted) {
			mDateStarted = new Date(pref.getLong("time_started", 0));
		}
	}

	private void saveData() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt("spent", mSpentToday);
		edit.putBoolean("started", mStarted);
		if (mStarted) {
			edit.putLong("time_started", System.currentTimeMillis());
		}
		edit.commit();
	}

	private void updateTime() {
		int time = mSpentToday;
		time += getStartedTaskTime();
		mSpentTimeTextView.setText(String.format("%1$d hrs %2$d min %3$d sec", time / 3600, (time % 3600)/60, time % 60));
	}

	private void updateStatus() {
		mStatusView.setBackgroundResource(mStarted ? R.drawable.started_circle : R.drawable.stopped_circle);
		mStartButton.setText(mStarted ? R.string.stop_button : R.string.start_button);
		mStartButton.setBackgroundResource(mStarted ? R.drawable.stopped_circle : R.drawable.started_circle);
	}

}
