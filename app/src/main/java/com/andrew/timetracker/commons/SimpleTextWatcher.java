package com.andrew.timetracker.commons;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by andrew on 16.08.2016.
 */
public abstract class SimpleTextWatcher implements TextWatcher {

	protected abstract void onChange(String text);

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		onChange(s.toString());
	}

	@Override
	public void afterTextChanged(Editable s) {

	}
}
