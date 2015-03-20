package com.ruptech.chinatalk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.MainActivity;
import com.ruptech.dlmu.im.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.LoginTask;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;

public class LoginLoadingActivity extends Activity {
	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	/*
	 * 进入主画面
	 */
	public static void gotoMainActivity(final Activity currentActivity) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(currentActivity, MainActivity.class);
				currentActivity.startActivity(intent);
			}
		}, 1);
	}

	public static void loginSuccessHandler(Activity currentActivity) {
		App.userDAO.mergeUser(App.readUser());

		gotoMainActivity(currentActivity);
	}

	protected static LoginLoadingActivity instance;

	public static final String PREF_USERINFO_NAME = "pref_userinfo_name";

	public static final String PREF_USERINFO_PASS = "pref_userinfo_pass";

	// Tasks.
	private GenericTask mLoginTask;

	private final TaskListener mLoginTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onLoginPrepare();
				onLoginSuccess();
			} else {
				onLoginFailure(task.getMsg());
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onLoginBegin();
		}
	};

	private void doLogin(String username, String password, String encrypt) {
		if (mLoginTask != null
				&& mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mLoginTask = new LoginTask(username, password, Boolean.valueOf(encrypt));
		mLoginTask.setListener(mLoginTaskListener);
		mLoginTask.execute();
	}

	private String[] getLoginInfo() {
		// 1, from extras
		String[] loginInfo = getLoginInfoFromExtras();
		if (loginInfo != null)
			return loginInfo;

		// 2, from SharedPreference
		User user = PrefUtils.readUser();
		if (user != null) {
			loginInfo = new String[]{user.getUsername(), user.getPassword(),
					Boolean.TRUE.toString()};
		}
		return loginInfo;
	}

	private String[] getLoginInfoFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return null;
		} else {
			String username = extras.getString(PREF_USERINFO_NAME);
			if (username == null)
				return null;
			String password = extras.getString(PREF_USERINFO_PASS);
			String encrypt = Boolean.FALSE.toString();
			return new String[]{username, password, encrypt};
		}
	}

	private void gotoLoginGatingActivity() {
		Intent intent = new Intent(this, LoginGateActivity.class);
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;
		setContentView(R.layout.activity_login_loading);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				tryLogin();
			}
		}, 0);
	}

	private void onLoginBegin() {
		// Utils.showProgress(this, R.string.logging_in);
	}

	private void onLoginFailure(String msg) {
		Utils.dismissDialog(LoginActivity.progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		// Utils.doLogout(this);
		if (null == LoginActivity.instance) {
			gotoLoginGatingActivity();
		}
		this.finish();
	}

	private void onLoginPrepare() {
	}

	private void onLoginSuccess() {
		Utils.dismissDialog(LoginActivity.progressDialog);
		loginSuccessHandler(LoginLoadingActivity.this);
	}

	private void tryLogin() {
		String[] loginInfo = getLoginInfo();
		if (loginInfo == null) {
			Toast.makeText(this, R.string.incorrect_login_info,
					Toast.LENGTH_SHORT).show();
			finish();
		} else {
			String username = loginInfo[0];
			String password = loginInfo[1];
			String encrypt = loginInfo[2];

			doLogin(username, password, encrypt);
		}
	}
}