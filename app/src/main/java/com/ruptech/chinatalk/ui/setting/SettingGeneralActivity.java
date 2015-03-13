package com.ruptech.chinatalk.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingGeneralActivity extends ActionBarActivity {
	private final String TAG = Utils.CATEGORY
			+ SettingGeneralActivity.class.getSimpleName();

	@InjectView(R.id.activity_setting_message_notification_slipswitch)
	ToggleButton messageNotificationSlipswitch;
	@InjectView(R.id.activity_setting_comment_notification_slipswitch)
	ToggleButton commentNotificationSlipswitch;
	@InjectView(R.id.activity_setting_reply_notification_slipswitch)
	ToggleButton replyNotificationSlipswitch;
	@InjectView(R.id.activity_setting_like_notification_slipswitch)
	ToggleButton likeNotificationSlipswitch;
	@InjectView(R.id.activity_setting_friend_notification_slipswitch)
	ToggleButton friendNotificationSlipswitch;
	@InjectView(R.id.activity_setting_translate_notification_slipswitch)
	ToggleButton translateNotificationSlipswitch;
	@InjectView(R.id.activity_setting_not_interrupt_switch_slipswitch)
	ToggleButton interruptTimeSlipswitch;
	@InjectView(R.id.activity_setting_not_interrupt_time_textview)
	TextView interruptTimeText;
	@InjectView(R.id.activity_setting_tts_slipswitch)
	ToggleButton ttsSlipswitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_general);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.new_message_come);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Utils.onBackPressed(this);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		Utils.onBackPressed(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		int start = PrefUtils.getPrefTranslatedNoticeInterruptStartHour();
		int duration = PrefUtils.getPrefTranslatedNoticeInterruptDuration();
		interruptTimeText.setText(Utils.getTimeSetting(this, start, duration));
		boolean pref_translated_interrupt_switch = PrefUtils
				.getPrefTranslatedNoticeInterruptSwitch();
		interruptTimeSlipswitch.setChecked(pref_translated_interrupt_switch);
	}

	@OnClick(R.id.activity_setting_not_interrupt_time_layout)
	public void setTime(View view) {
		Intent intent = new Intent(this, SettingGeneralTimeActivity.class);
		startActivity(intent);
	}

	private void setupComponents() {
		boolean pref_translated_notice_message = PrefUtils
				.getPrefTranslatedNoticeMessage();

		messageNotificationSlipswitch
				.setChecked(pref_translated_notice_message);
		messageNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeMessage(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeMessage(false);
						}
					}
				});

		boolean pref_translated_notice_comment = PrefUtils
				.getPrefTranslatedNoticeComment();

		commentNotificationSlipswitch
				.setChecked(pref_translated_notice_comment);
		commentNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeComment(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeComment(false);
						}
					}
				});

		boolean pref_translated_notice_reply = PrefUtils
				.getPrefTranslatedNoticeReply();

		replyNotificationSlipswitch.setChecked(pref_translated_notice_reply);
		replyNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeReply(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeReply(false);
						}
					}
				});

		boolean pref_translated_notice_like = PrefUtils
				.getPrefTranslatedNoticeLike();

		likeNotificationSlipswitch.setChecked(pref_translated_notice_like);
		likeNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeLike(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeLike(false);
						}
					}
				});

		boolean pref_translated_notice_friend = PrefUtils
				.getPrefTranslatedNoticeFriend();

		friendNotificationSlipswitch.setChecked(pref_translated_notice_friend);
		friendNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeFriend(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeFriend(false);
						}
					}
				});

		boolean pref_translated_notice_translate = PrefUtils
				.getPrefTranslatedNoticeTranslate();

		translateNotificationSlipswitch
				.setChecked(pref_translated_notice_translate);
		translateNotificationSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							PrefUtils.savePrefTranslatedNoticeTranslate(true);
						} else {
							PrefUtils.savePrefTranslatedNoticeTranslate(false);
						}
					}
				});

		boolean pref_translated_notice_tts = PrefUtils
				.getPrefTranslatedNoticeTts();

		ttsSlipswitch.setChecked(pref_translated_notice_tts);
		ttsSlipswitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					PrefUtils.savePrefTranslatedNoticeTts(true);
				} else {
					PrefUtils.savePrefTranslatedNoticeTts(false);
				}
			}
		});

		boolean pref_translated_interrupt_switch = PrefUtils
				.getPrefTranslatedNoticeInterruptSwitch();
		interruptTimeSlipswitch.setChecked(pref_translated_interrupt_switch);
		interruptTimeSlipswitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						PrefUtils
								.savePrefTranslatedNoticeInterruptSwitch(isChecked);
					}
				});

	}
}
