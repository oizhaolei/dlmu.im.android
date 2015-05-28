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

import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingGeneralActivity extends ActionBarActivity {
    private final String TAG = Utils.CATEGORY
            + SettingGeneralActivity.class.getSimpleName();

    @InjectView(R.id.activity_setting_message_notification_slipswitch)
    ToggleButton messageNotificationSlipswitch;
    @InjectView(R.id.activity_setting_not_interrupt_switch_slipswitch)
    ToggleButton interruptTimeSlipswitch;
    @InjectView(R.id.activity_setting_not_interrupt_time_textview)
    TextView interruptTimeText;

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
            onBackPressed();
        }
        return true;
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
