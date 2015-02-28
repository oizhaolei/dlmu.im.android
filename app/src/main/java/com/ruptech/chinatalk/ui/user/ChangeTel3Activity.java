package com.ruptech.chinatalk.ui.user;

import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.SendVerifyCodeSmsTask;
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.task.impl.VerifyCodeVerifyTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

/**
 * Activity which displays a change tel screen to the user, offering
 * registration as well.
 */
public class ChangeTel3Activity extends ActionBarActivity {

	class DelayEnableEntryTimerTask extends TimerTask {
		@Override
		public void run() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					delayEnableEntry();
				}
			});
		}

	}

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private final TaskListener mSmsSendTaskListener = new TaskAdapter() {


		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onSmsSendSuccess();
			} else {
				String msg = task.getMsg();
				verifyCodeButton.setEnabled(false);
				onSmsSendFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onSmsVerifyBegin();
		}
	};

	private final TaskListener mUserChangeTelListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserProfileChangeTask userProfileChangeTask = (UserProfileChangeTask) task;
			if (result == TaskResult.OK) {
				User user = userProfileChangeTask.getUser();
				onTelChangeSuccess(user);
			} else {
				String msg = userProfileChangeTask.getMsg();
				onTelChangeFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onTelChangeBegin();
		}

	};
	private final String TAG = Utils.CATEGORY
			+ ChangeTel3Activity.class.getSimpleName();

	@InjectView(R.id.activity_change_tel_step3_verify_code)
	EditText mVerifyCodeEditText;

	private String mTel;
	@InjectView(R.id.activity_change_tel_step3_user_tel)
	TextView mUserTelTextView;

	@InjectView(R.id.activity_change_tel_step3_send_verify)
	Button sendVverifyCodeButton;

	@InjectView(R.id.activity_change_tel_step3_verify_button)
	Button verifyCodeButton;

	@InjectView(R.id.activity_change_tel_step3_verify_error)
	TextView mVerifyCodeErrroTextView;

	private int mCountdown;

	private final Handler mHandler = new Handler();

	private Timer delayEnableEntryTimer;

	private static ChangeTel3Activity instance;
	// Tasks.
	private GenericTask mSmsSendTask;

	private GenericTask mTelChangeTask;

	private GenericTask mVerifyCodeVerifyTask;

	private final TaskListener mDoVerifyTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				doVerifySuccess();
			} else {
				doVerifyFailure(task.getMsg());
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			doVerifyBegin();
		}

	};

	private final int VERIFY_CODE_LENGTH = 6;

	private ProgressDialog progressDialog;

	private void delayEnableEntry() {
		mCountdown--;
		if (mCountdown == 0) {
			sendVverifyCodeButton.setEnabled(true);
			sendVverifyCodeButton
			.setText(getString(R.string.prompt_change_tel_step3_send_verify_code_again));
			delayEnableEntryTimer.cancel();
		} else {
			sendVverifyCodeButton.setText(getString(
					R.string.prompt_change_tel_step3_send_verify_code_again2,
					mCountdown));
		}
	}

	private void disableEntry() {
		mVerifyCodeEditText.setEnabled(false);
		mVerifyCodeErrroTextView.setVisibility(View.GONE);
		sendVverifyCodeButton.setEnabled(false);
		verifyCodeButton.setEnabled(false);
	}

	@OnClick(R.id.activity_change_tel_step3_send_verify)
	public void doSendSms(View v) {
		if (mSmsSendTask != null && mSmsSendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		if (!TextUtils.isEmpty(mTel)) {
			mSmsSendTask = new SendVerifyCodeSmsTask(mTel);
			mSmsSendTask.setListener(mSmsSendTaskListener);
			mSmsSendTask.execute();
		}
	}

	@OnClick(R.id.activity_change_tel_step3_verify_button)
	public void doVerify(View v) {
		mVerifyCodeVerifyTask = new VerifyCodeVerifyTask(mTel,
				mVerifyCodeEditText.getText().toString());
		mVerifyCodeVerifyTask.setListener(mDoVerifyTaskListener);
		mVerifyCodeVerifyTask.execute();
	}
	private void doVerifyBegin() {
		progressDialog = Utils.showDialog(this, getString(R.string.verify_processing));
		disableEntry();
	}

	private void doVerifyFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		enableEntry();

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		mVerifyCodeErrroTextView.setVisibility(View.VISIBLE);
	}

	private void doVerifySuccess() {
		Utils.dismissDialog(progressDialog);
		mVerifyCodeErrroTextView.setVisibility(View.INVISIBLE);
		mTelChangeTask = new UserProfileChangeTask("change_column", "tel", mTel);
		mTelChangeTask.setListener(mUserChangeTelListener);
		mTelChangeTask.execute();
	}

	private void enableEntry() {
		mVerifyCodeEditText.setEnabled(true);
	}

	private void extras() {
		Bundle extras = getIntent().getExtras();
		mTel = extras.getString(ChangeTel2Activity.EXTRA_MOBILE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		instance = this;
		setContentView(R.layout.change_tel_step3);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.modified_tel);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		extras();
		setupComponents();

		doSendSms(null);
	}

	@Override
	protected void onDestroy() {
		instance = null;
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void onSmsSendFailure(String msg) {
		Utils.dismissDialog(progressDialog);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
	}

	private void onSmsSendSuccess() {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.action_send_verify_code_ok, Toast.LENGTH_LONG).show();
		enableEntry();
		mCountdown = AppPreferences.SEND_SMS_CODE_REPEAT;
		delayEnableEntryTimer = new Timer();
		delayEnableEntryTimer.schedule(new DelayEnableEntryTimerTask(), 0, 1000);
	}

	private void onSmsVerifyBegin() {
		progressDialog = Utils.showDialog(this,
				getString(R.string.action_send_verify_code_beagin));
		disableEntry();
	}

	private void onTelChangeBegin() {
		progressDialog = Utils.showDialog(this,
				getString(R.string.verify_processing));
		disableEntry();
	}

	private void onTelChangeFailure(String msg) {
		progressDialog.dismiss();

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
	}

	private void onTelChangeSuccess(User user) {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.modified_tel_success, Toast.LENGTH_LONG)
				.show();
		enableEntry();

		ChangeTel1Activity.close();
		ChangeTel2Activity.close();
		ProfileActivity.close();

		Intent intent = new Intent(this, FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		this.startActivity(intent);

		this.finish();
	}
	private void setupComponents() {
		mVerifyCodeEditText
		.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				VERIFY_CODE_LENGTH) });

		mUserTelTextView.setText(getString(R.string.modified_tel_is, mTel));

		mVerifyCodeEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() == VERIFY_CODE_LENGTH) {
					verifyCodeButton.setEnabled(true);
				} else {
					verifyCodeButton.setEnabled(false);
				}
			}
		});
	}
}
