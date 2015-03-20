package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class FullScreenActivity extends Activity {
	@InjectView(R.id.activity_fullscren_message)
	TextView mMessage;
	public static final String EXTRA_MESSAGE = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		ButterKnife.inject(this);

		setupComponments();
	}

	private void setupComponments() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String msg = extras.getString(EXTRA_MESSAGE);
			mMessage.setText(msg);
		}
	}
}
