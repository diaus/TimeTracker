package com.andrew.timetracker.views;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.andrew.timetracker.R;
import com.andrew.timetracker.views.tasks.TasksFragment;
import com.andrew.timetracker.views.home.HomeFragment;


public class MainActivity extends AppCompatActivity implements IMainActivity {
	private static final String TAG = "tt: MainActivity";

	public interface ITab {
		void onTabSelected();
	}

	private ViewPager mViewPager;
	private FragmentStatePagerAdapter mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mViewPager = (ViewPager) findViewById(R.id.activity_main_tabs_pager);
		FragmentManager fragmentManager = getSupportFragmentManager();
		mAdapter = new FragmentStatePagerAdapter(fragmentManager) {
			@Override
			public Fragment getItem(int position) {
				Log.d(TAG, "create tab fragment " + position);
				switch (position){
					case 0: return new HomeFragment();
					case 1: return new TasksFragment();
				}
				return null;
			}
			@Override
			public int getCount() {
				return 2;
			}
		};

		mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position) {
				Log.d(TAG, "tab selected " + position);
				getTab(position).onTabSelected();
				clearInvalidations();
			}
		});
		mViewPager.setAdapter(mAdapter);

		Log.d(TAG, "end of onCreate in main activity");

	}

	private ITab getTab(int position){
		return (ITab) mAdapter.instantiateItem(mViewPager, position);
	}

	private ITab getCurrentTab(){
		return getTab(mViewPager.getCurrentItem());
	}

	@Override
	public void switchToHomeTab() {
		mViewPager.setCurrentItem(0);
	}

	private boolean _isInvalidatedTask;
	private boolean _isInvalidatedTimelines;

	private void clearInvalidations(){
		_isInvalidatedTimelines = false;
		_isInvalidatedTask = false;
	}

	@Override
	public void invalidateTask() {
		_isInvalidatedTask = true;
	}

	@Override
	public void invalidateTimelines() {
		_isInvalidatedTimelines = true;
	}

	@Override
	public boolean isInvalidatedTask() {
		return _isInvalidatedTask;
	}

	@Override
	public boolean isInvalidatedTimelines() {
		return _isInvalidatedTimelines;
	}

}
