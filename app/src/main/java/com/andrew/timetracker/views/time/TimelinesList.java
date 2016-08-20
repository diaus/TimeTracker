package com.andrew.timetracker.views.time;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andrew.timetracker.R;
import com.andrew.timetracker.database.Timeline;
import com.andrew.timetracker.utils.helper;

/**
 * Created by andrew on 20.08.2016.
 */
public class TimelinesList extends TimeListBase<TimelinesList.ItemHolder> {

	class ItemHolder {

	}

	@Override
	protected void createViews() {
		Context context = getContext();
		for (Timeline tl : mTimelines){
			ItemHolder holder = new ItemHolder();
			View v = inflateItem(R.layout.time_timelines_item, holder);

			TextView period = (TextView) v.findViewById(R.id.time_timelines_item_period);
			TextView time = (TextView) v.findViewById(R.id.time_timelines_item_time);

			period.setText(helper.formatShortTime(tl.getStartTime(), true) + " - " + (tl.getStopTime() == null ? "now" : helper.formatShortTime(tl.getStopTime(), true)));

			time.setText(helper.formatShortSpentTime(context, tl.getSpentSeconds()));
			time.setTypeface(null, Typeface.BOLD_ITALIC);

			this.addView(v);
		}
	}

	@Override
	protected void onItemClick(ViewGroup v, ItemHolder itemHolder) {

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
