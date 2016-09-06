package com.andrew.timetracker.views;

import android.support.v4.app.Fragment;

/**
 * Created by andrew on 06.09.2016.
 */
public abstract class MainActivityTabFragment extends Fragment implements MainActivity.ITab {
	@Override
	public IMainActivity getActivityMain() {
		return (IMainActivity) getActivity();
	}

	@Override
	public boolean doBack() {
		return false;
	}
}
