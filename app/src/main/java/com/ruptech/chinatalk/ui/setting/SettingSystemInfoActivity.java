package com.ruptech.chinatalk.ui.setting;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.event.LogoutEvent;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class SettingSystemInfoActivity extends ActionBarActivity {

	private final String TAG = Utils.CATEGORY
			+ SettingSystemInfoActivity.class.getSimpleName();


	// 退出
	public void doSystemLogout(MenuItem item) {
		DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				Utils.doLogout(App.mContext);
			}
		};
		DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		};
		Utils.AlertDialog(this, positiveListener, negativeListener,
				this.getString(R.string.logout),
				this.getString(R.string.tip_logout));
	}


	private ProgressDialog progressDialog;

	@Subscribe
	public void answerLogout(LogoutEvent event) {
		finish();
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	// 系统url
	@OnClick(R.id.main_tab_setting_show_system_url_textview)
	public void doShowSystemUrl(View v) {

		//
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
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_actions, mMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}


	// 翻译设定
	@OnClick(R.id.activity_setting_general_rl)
	public void setting_general(View v) {
		Intent intent = new Intent(this, SettingGeneralActivity.class);
		startActivity(intent);
	}


	private void setupComponents() {
	}

}