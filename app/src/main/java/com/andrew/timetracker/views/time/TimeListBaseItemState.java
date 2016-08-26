package com.andrew.timetracker.views.time;

import java.io.Serializable;
import java.util.List;

/**
 * Created by andrew on 26.08.2016.
 */

public class TimeListBaseItemState implements Serializable {
	public Long key;
	public List<TimeListBaseItemState> childState;

	public TimeListBaseItemState() {
	}

	public TimeListBaseItemState(Long key, List childState) {
		this.childState = childState;
		this.key = key;
	}
}