package com.andrew.timetracker.commons;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 25.08.2016.
 */
public class SimpleListView extends LinearLayout {

	public interface IAdapter<ItemViewHolder> {
		int getItemCount();

		void bindViewHolder(ItemViewHolder holder, int position);

		ItemViewHolder createViewHolder(View v);
	}

	IAdapter mAdapter;
	List<View> mViews = new ArrayList<>();
	int mItemLayoutResourceId;

	public void setAdapter(IAdapter adapter, @LayoutRes int itemLayoutResourceId) {
		mAdapter = adapter;
		mItemLayoutResourceId = itemLayoutResourceId;
		updateData();
	}

	public void updateItem(int position) {
		mAdapter.bindViewHolder(mViews.get(position).getTag(), position);
	}

	public void updateData() {
		int count = mAdapter.getItemCount();
		int viewsCount = mViews.size();
		if (count < viewsCount) {
			for (int i = viewsCount - 1; i >= count; i--) {
				View v = mViews.get(i);
				mViews.remove(i);
				this.removeView(v);
			}
		}
		for (int i = viewsCount; i < count; i++) {
			View v = createItemView();
			Object holder = mAdapter.createViewHolder(v);
			v.setTag(holder);
			mViews.add(v);
			this.addView(v);
		}
		viewsCount = mViews.size();
		for (int i = 0; i < viewsCount; i++) {
			Object holder = mViews.get(i).getTag();
			mAdapter.bindViewHolder(holder, i);
		}
	}

	private View createItemView() {
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		return layoutInflater.inflate(mItemLayoutResourceId, null);
	}

	public SimpleListView(Context context) {
		super(context);
	}

	public SimpleListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SimpleListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SimpleListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
}
