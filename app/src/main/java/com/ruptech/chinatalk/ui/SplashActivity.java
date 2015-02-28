package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.VersionCheckTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyNotificationBuilder;

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
			notificateUpdateVersion();
		}
	}

	private void doCheckServerInfo() {
		VersionCheckTask mVersionCheckTask = new VersionCheckTask();
		mVersionCheckTask.setListener(serverInfoCheckTaskListener);
		mVersionCheckTask.execute();
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

	private void notificateUpdateVersion() {
		String content = getString(R.string.please_click_to_update_newapk);
		int defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND;
		long when = System.currentTimeMillis();
		NotificationCompat.Builder mBuilder = new MyNotificationBuilder(this)
				.setSmallIcon(R.drawable.ic_tttalk_gray_light)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_launcher))
				.setContentTitle(getString(R.string.app_name))
				.setTicker(content).setContentText(content)
				.setVibrate(AppPreferences.NOTIFICATION_VIBRATE)
				.setDefaults(defaults).setAutoCancel(true).setWhen(when)
				.setShowSetting(false);
		Intent notificationIntent = UpdateVersionServiceActivity
				.createIntent(getApplicationContext());
		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, notificationIntent, 0);
		mBuilder.setContentIntent(contentIntent);
		App.notificationManager.cancel(R.string.new_version_loading_begin);
		App.notificationManager.cancel(R.string.please_click_to_update_newapk);
		App.notificationManager.notify(R.string.please_click_to_update_newapk,
				mBuilder.build());
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

		if (Utils.checkNetwork(this)) {
			doCheckServerInfo();
		} else {
			gotoDispatchActivity();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void setupComponents() {
		footerTextView.setText(getString(R.string.gate_footer_text,
				App.mApkVersionOfClient.verName));
	}
}