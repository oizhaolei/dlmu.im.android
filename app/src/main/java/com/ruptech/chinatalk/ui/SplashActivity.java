package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveServerVersionTask;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.dlmu.im.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SplashActivity extends Activity {

	public static SplashActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	public boolean directlyToMain = false;

	private final TaskListener serverInfoCheckTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (App.readServerAppInfo() == null) {
				gotoLogiGateActivity();
			} else {
				PrefUtils.saveVersionCheckedTime();
				App.versionChecked = true;
				PrefUtils.savePrefLastApkVersion();

				checkVersion();// 通知栏提醒版本更新
				gotoDispatchActivity();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
		}

	};

	@InjectView(R.id.activity_splash_footer_textview)
	TextView footerTextView;


	private void checkVersion() {
		if (App.readServerAppInfo() != null
				&& App.readServerAppInfo().verCode > App.mApkVersionOfClient.verCode) {
			App.getApkUpgrade(this).checkApkUpdate(false);
		}
	}

	private void doCheckServerInfo() {
		RetrieveServerVersionTask mRetrieveServerVersionTask = new RetrieveServerVersionTask();
		mRetrieveServerVersionTask.setListener(serverInfoCheckTaskListener);
		mRetrieveServerVersionTask.execute();
	}

	private void gotoDispatchActivity() {
		this.directlyToMain = false;
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				// usage demo
				if (PrefUtils.existsPrefUserInfo()) {
					gotoLoginLoadingActivity();
				} else {
					gotoLogiGateActivity();
				}
			}
		});
	}

	private void gotoLogiGateActivity() {
		Intent intent = new Intent(SplashActivity.this, LoginGateActivity.class);
		startActivity(intent);
	}

	private void gotoLoginLoadingActivity() {
		Intent intent = new Intent(SplashActivity.this,
				LoginLoadingActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);
		ButterKnife.inject(this);

		if (App.isAvailableShowMain()
				&& PrefUtils.getPrefLastApkVersion() == Utils
				.getAppVersionCode()
				&& App.readServerAppInfo().verCode == Utils.getAppVersionCode()) {

			this.directlyToMain = true;
			LoginLoadingActivity.gotoMainActivity(this);
			finish();
			return;
		}

		setupComponents();

		instance = this;
		App.taskManager.cancelAll();

	}

	@Override
	public void onResume() {
		super.onResume();
		if (Utils.checkNetwork(this)) {
			doCheckServerInfo();
		} else {
			gotoDispatchActivity();
		}
	}

	private void setupComponents() {
		footerTextView.setText(getString(R.string.gate_footer_text,
				App.mApkVersionOfClient.verName));
	}
}