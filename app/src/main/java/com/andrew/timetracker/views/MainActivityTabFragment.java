package com.andrew.timetracker.views;

import android.support.v4.app.Fragment;

import com.andrew.timetracker.database.TaskDao;
import com.andrew.timetracker.database.TimelineDao;
import com.andrew.timetracker.events.DbChangesEvent;
import com.andrew.timetracker.utils.helper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by andrew on 06.09.2016.
 */
public abstract class MainActivityTabFragment extends Fragment implements MainActivity.ITab {

	protected void postDbChange(){
		helper.postDbChange(this);
	}

	protected boolean shouldInvalidate;

	protected void onDbChange(){
		// called on db change event
	}

	@Subscribe
	public void handleDbChangeEvent(DbChangesEvent event) {
		if (event.sender == this) return;
		shouldInvalidate = true;
		onDbChange();
	}

	@Override
	public IMainActivity getActivityMain() {
		return (IMainActivity) getActivity();
	}

	@Override
	public boolean doBack() {
		return false;
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	protected TimelineDao timelineDao(){
		return getActivityMain().getTimelineDao();
	}

	protected TaskDao taskDao(){
		return getActivityMain().getTaskDao();
	}
}
