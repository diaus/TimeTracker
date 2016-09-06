package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.events.DbChangesEvent;
import com.andrew.timetracker.utils.helper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 20.08.2016.
 */
public class TimelinesList extends TimeListBase<Long, TimelinesList.ItemHolder> {

	ItemHolder mSelectedItemHolder = null;

	@Override
	public List<TimeListBaseItemState> getOpenedState() {
		if (mSelectedItemHolder == null) return null;
		TimeListBaseItemState itemState = new TimeListBaseItemState(mSelectedItemHolder.timeline.getId(), null);
		List<TimeListBaseItemState> state = new ArrayList<>();
		state.add(itemState);
		return state;
	}

	@Override
	public void restoreOpenedState(List<TimeListBaseItemState> state) {
		if (state == null || state.size() != 1) return;
		TimeListBaseItemState itemState = state.get(0);
		mSelectedItemHolder = mItemHolders.get(itemState.key);
		updateHolder(mSelectedItemHolder);
	}

	@Override
	protected TimeListBase createChild(ItemHolder holder) {
		return null;
	}

	class ItemHolder extends TimeListBase.ItemHolder {
		public Timeline timeline;

		public ItemHolder(Timeline timeline) {
			this.timeline = timeline;
		}

		public void init() {
			ImageButton btnDelete = (ImageButton) view.findViewById(R.id.time_timelines_item_delete_button);
			btnDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(getContext())
							  .setMessage(getResources().getString(R.string.confirm_delete_timeline)
										 + "\n" + helper.formatTimelinePeriod(timeline, true, getContext())
										 + " [ " + helper.formatSpentTime(getContext(), timeline.getSpentSeconds(), true) + " ]"
										 + "\n" + getTask(timeline.getTaskId()).getName())
							  .setTitle(R.string.confirm_dialog_title)
							  .setIcon(R.drawable.icon_alert)
							  .setNegativeButton(android.R.string.cancel, null)
							  .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
								  @Override
								  public void onClick(DialogInterface dialog, int which) {
									  mTimelineDao.delete(timeline);
									  helper.postDbChange(this);
									  mEventHandler.invalidate();
								  }
							  })
							  .show();
				}
			});

			ImageButton btnEdit = (ImageButton) view.findViewById(R.id.time_timelines_item_edit_button);
			btnEdit.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mEventHandler.editTimeline(timeline);
				}
			});
		}
	}

	@Override
	protected void createViews() {
		Context context = getContext();
		for (Timeline tl : mTimelines){
			ItemHolder holder = new ItemHolder(tl);
			View v = inflateItem(R.layout.time_timelines_item, holder);
			mItemHolders.put(tl.getId(), holder);

			TextView period = (TextView) v.findViewById(R.id.time_timelines_item_period);
			period.setText(helper.formatTimelinePeriod(tl, false, getContext()));

			TextView time = (TextView) v.findViewById(R.id.time_timelines_item_time);
			time.setText(helper.formatSpentTime(context, tl.getSpentSeconds(), true));
			time.setTypeface(null, Typeface.BOLD_ITALIC);

			if (mSelectedTask == null){
				TextView task = (TextView)v.findViewById(R.id.time_timelines_item_task);
				task.setVisibility(VISIBLE);
				task.setText(getTask(tl.getTaskId()).getName());
			}

			this.addView(v);

			holder.init();
		}
	}

	@Override
	public void onClick(View v) {
		ItemHolder holder = (ItemHolder) v.getTag();
		if (holder == mSelectedItemHolder) return;
		ItemHolder prevHolder = mSelectedItemHolder;
		mSelectedItemHolder = holder;
		if (prevHolder != null){
			updateHolder(prevHolder);
		}
		updateHolder(holder);
	}

	private void updateHolder(ItemHolder holder) {
		if (holder == null) return;
		boolean isSelected = holder == mSelectedItemHolder;
		View mContainer = holder.view.findViewById(R.id.time_timelines_item_container);
		mContainer.setBackgroundResource(isSelected ? R.drawable.timeline_selected_bg : 0);
		mContainer.findViewById(R.id.time_timelines_item_delete_button).setVisibility(isSelected ? VISIBLE : GONE);
		mContainer.findViewById(R.id.time_timelines_item_edit_button).setVisibility(isSelected ? VISIBLE : GONE);
	}

	public TimelinesList(Context context) {
		super(context);
	}

	public TimelinesList(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TimelinesList(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public TimelinesList(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

}
