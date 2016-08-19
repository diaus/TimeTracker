package com.andrew.timetracker.views;

/**
 * Created by andrew on 18.08.2016.
 */
public interface IMainActivity {
	void switchToHomeTab();
	void invalidateTask();
	void invalidateTimelines();
	boolean isInvalidatedTask();
	boolean isInvalidatedTimelines();
}
