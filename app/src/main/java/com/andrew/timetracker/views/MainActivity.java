package com.andrew.timetracker.views;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.andrew.timetracker.R;
import com.andrew.timetracker.commons.IBackPressedListener;
import com.andrew.timetracker.views.home.HomeFragment;
import com.andrew.timetracker.views.tasks.TasksFragment;
import com.andrew.timetracker.views.time.TimeFragment;


public class MainActivity extends AppCompatActivity implements IMainActivity {
	private static final String TAG = "tt: MainActivity";

	public interface ITab extends IBackPressedListener {
		void onTabSelected();
		IMainActivity getActivityMain();
	}

	private DrawerLayout mDrawerLayout;

	private ViewPager mViewPager;
	private FragmentStatePagerAdapter mAdapter;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
		actionBar.setDisplayHomeAsUpEnabled(true);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mViewPager = (ViewPager) findViewById(R.id.tabs_pager);
		FragmentManager fragmentManager = getSupportFragmentManager();
		mAdapter = new FragmentStatePagerAdapter(fragmentManager) {
			@Override
			public Fragment getItem(int position) {
				Log.d(TAG, "create tab fragment " + position);
				switch (position){
					case 0: return new HomeFragment();
					case 1: return new TimeFragment();
					case 2: return new TasksFragment();
				}
				return null;
			}
			@Override
			public int getCount() {
				return 3;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				switch (position){
					case 0: return getString(R.string.tab_title_home);
					case 1: return getString(R.string.tab_title_stats);
					case 2: return getString(R.string.tab_title_tasks);
				}
				return null;
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

		TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs_layout);
		tabLayout.setupWithViewPager(mViewPager);

		Log.d(TAG, "end of onCreate in main activity");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
			case android.R.id.home:
				mDrawerLayout.openDrawer(GravityCompat.START);
				return true;
		}
		return super.onOptionsItemSelected(item);
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

	@Override
	public void onBackPressed() {
		ITab tab = getCurrentTab();
		if (tab.doBack()){
			// processed by tab
			return;
		}
		if (mViewPager.getCurrentItem() != 0){
			switchToHomeTab();
		} else {
			super.onBackPressed();
		}
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
