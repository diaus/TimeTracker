package com.andrew.timetracker;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
	private static final String TAG = "tt: MainActivity";

	public interface ITab {
		//void onTabSelected();
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
					case 0: return new TabHomeFragment();
					case 1: return new TabTasksFragment();
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
			}
		});

		mViewPager.setAdapter(mAdapter);

		Log.d(TAG, "end of onCreate in main activity");

	}

	private ITab getCurrentTab(){
		return (ITab) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());
	}

}
