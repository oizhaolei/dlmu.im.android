package com.ruptech.chinatalk.ui.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.ui.ChatActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingSystemInfoActivity extends ActionBarActivity {

    private final String TAG = Utils.CATEGORY
            + SettingSystemInfoActivity.class.getSimpleName();

    @InjectView(R.id.activity_setting_view_not_receive_slipswitch)
    ToggleButton notReceiveSlipswitch;

    @InjectView(R.id.activity_setting_view_verification_slipswitch)
    ToggleButton verificationSlipswitch;


    @Subscribe
    public void answerLogout(LogoutEvent event) {
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_system_info);
        ButterKnife.inject(this);
        App.mBus.register(this);

        getSupportActionBar().setTitle(R.string.setting);
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

    private void setupComponents() {
        boolean pref_not_receive_message = PrefUtils
                .getPrefNotReceiveMessage();
        boolean pref_verification_message = PrefUtils
                .getPrefVerificationMessage();

        notReceiveSlipswitch
                .setChecked(pref_not_receive_message);
        notReceiveSlipswitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            PrefUtils.savePrefNotReceiveMessage(true);
                            verificationSlipswitch.setChecked(false);
                        } else {
                            PrefUtils.savePrefNotReceiveMessage(false);
                        }
                    }
                });

        verificationSlipswitch
                .setChecked(pref_verification_message);
        verificationSlipswitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            PrefUtils.savePrefVerificationeMessage(true);
                            notReceiveSlipswitch.setChecked(false);
                        } else {
                            PrefUtils.savePrefVerificationeMessage(false);
                        }
                    }
                });
    }
    @OnClick(R.id.activity_setting_view_blocklist_layout)
    public void gotoBlockList() {
        Intent intent = new Intent(this, BlockedUserListActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.activity_setting_view_show_system_url_textview)
    public void freeChat() {
        final EditText idEditText = new EditText(this);


        new AlertDialog.Builder(this)
                .setView(idEditText)
                .setPositiveButton("Moustachify", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String userId = idEditText.getText().toString();

                        String jid = userId + "@" + AppPreferences.IM_SERVER_RESOURCE;
                        Intent chatIntent = new Intent(SettingSystemInfoActivity.this, ChatActivity.class);
                        chatIntent.putExtra(ChatActivity.EXTRA_JID, jid);
                        chatIntent.putExtra(ChatActivity.EXTRA_TITLE, jid);
                        startActivity(chatIntent);
                    }
                })
                .show();
    }
}