package com.andrew.timetracker.views.time;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrew.timetracker.R;
import com.andrew.timetracker.views.MainActivity;

/**
 * Created by andrew on 19.08.2016.
 */
public class TimeFragment extends Fragment implements MainActivity.ITab {

	@Override
	public void onTabSelected() {

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_time, container, false);

		return v;
	}
}
