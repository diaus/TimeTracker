package com.andrew.timetracker.commons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.andrew.timetracker.R;

/**
 * Created by andrew on 15.08.2016.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

	protected abstract Fragment createFragment();
	protected abstract int fragmentContainerId();
	protected abstract void setContentView();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView();

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(fragmentContainerId());
		if (fragment == null) {
			fragment = createFragment();
			fm.beginTransaction()
					  .add(fragmentContainerId(), fragment)
					  .commit();
		}
	}


	protected String TAG(){
		return getClass().getSimpleName();
	}

}
