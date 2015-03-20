package com.ruptech.chinatalk.ui.setting;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SettingGeneralTimeActivity extends ActionBarActivity {

	private final String TAG = Utils.CATEGORY
			+ SettingGeneralTimeActivity.class.getSimpleName();
	@InjectView(R.id.interrupt_result_textview)
	TextView resultText;
	@InjectView(R.id.interrupt_start_textview)
	TextView startText;
	@InjectView(R.id.interrupt_duration_textview)
	TextView durationText;
	@InjectView(R.id.start_time_seekBar)
	SeekBar startTimeSeek;
	@InjectView(R.id.duration_seekBar)
	SeekBar durationSeek;

	@Override
	public void onBackPressed() {
		PrefUtils.savePrefTranslatedNoticeInterruptStartHour(startTimeSeek
				.getProgress());
		PrefUtils.savePrefTranslatedNoticeInterruptDuration(durationSeek
				.getProgress());
		if (durationSeek.getProgress() == 0) {
			PrefUtils.savePrefTranslatedNoticeInterruptSwitch(false);
		} else {
			PrefUtils.savePrefTranslatedNoticeInterruptSwitch(true);
		}
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_general_time);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.not_interrupt_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	private void setupComponents() {
		OnSeekBarChangeListener listener = new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
			                              boolean fromUser) {
				update();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

		};
		startTimeSeek.setOnSeekBarChangeListener(listener);
		durationSeek.setOnSeekBarChangeListener(listener);

		int startHour = PrefUtils.getPrefTranslatedNoticeInterruptStartHour();
		int duration = PrefUtils.getPrefTranslatedNoticeInterruptDuration();

		startTimeSeek.setProgress(startHour);
		durationSeek.setProgress(duration);

		startText.setText(this.getString(R.string.not_interrupt_start,
				startHour));
		durationText.setText(this.getString(R.string.not_interrupt_duration,
				duration));
		resultText.setText(Utils.getTimeSetting(this, startHour, duration));
	}

	private void update() {
		int startHour = startTimeSeek.getProgress();
		int duration = durationSeek.getProgress();
		startText.setText(this.getString(R.string.not_interrupt_start,
				startHour));
		durationText.setText(this.getString(R.string.not_interrupt_duration,
				duration));
		resultText.setText(Utils.getTimeSetting(this, startHour, duration));
	}
}
