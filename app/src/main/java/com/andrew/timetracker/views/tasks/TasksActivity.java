package com.andrew.timetracker.views.tasks;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.andrew.timetracker.R;

/**
 * Created by andrew on 19.08.2016.
 */
public class TasksActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tasks);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.single_frame_container);
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction()
					  .add(R.id.single_frame_container, fragment)
					  .commit();
		}
	}

	private Fragment createFragment() {
		return new TasksFragment();
	}
}
