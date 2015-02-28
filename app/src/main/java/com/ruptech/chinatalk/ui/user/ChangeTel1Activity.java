package com.ruptech.chinatalk.ui.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.LoginTask;
import com.ruptech.chinatalk.ui.dialog.AbstractUserActivity;
import com.ruptech.chinatalk.utils.Utils;

public class ChangeTel1Activity extends AbstractUserActivity {

	protected static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private ProgressDialog progressDialog;

	private final String TAG = Utils.CATEGORY
			+ ChangeTel1Activity.class.getSimpleName();

	@InjectView(R.id.activity_change_tel_pwd_button)
	EditText mPassWordEditText; // 当前密码
	@InjectView(R.id.activity_change_tel_step1_error)
	TextView mPassWordError;
	@InjectView(R.id.activity_change_tel_step1_next_button)
	Button checkPasswordBtn;
	private static ChangeTel1Activity instance;

	private GenericTask mLoginTask;

	private final TaskListener mLoginTaskListener = new TaskAdapter() {


		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onCheckPasswordSuccess();
			} else {
				onCheckPasswordFailure(task.getMsg());
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onCheckPasswordBegin();
		}

	};

	@OnClick(R.id.activity_change_tel_step1_next_button)
	public void checkPassword(View v) {
		String password = mPassWordEditText.getText().toString();
		if (Utils.isEmpty(password)) {
			mPassWordError.setVisibility(View.VISIBLE);
			mPassWordError.setText(R.string.pwd_is_null);
			return;
		}

		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mLoginTask = new LoginTask(mUser.getTel(), password, false);
		mLoginTask.setListener(mLoginTaskListener);
		mLoginTask.execute();
	}

	private  void gotoChangeTelStep2Activity(Activity context) {
		Intent intent = new Intent(context, ChangeTel2Activity.class);
		context.startActivity(intent);
	}

	private void onCheckPasswordBegin() {
		progressDialog = Utils.showDialog(this,
				getString(R.string.change_tel_input_pwd_check));
		mPassWordEditText.setEnabled(false);
		mPassWordError.setVisibility(View.GONE);
		checkPasswordBtn.setEnabled(false);
	}

	private void onCheckPasswordFailure(String msg) {
		Utils.dismissDialog(progressDialog);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		mPassWordEditText.setEnabled(true);
		mPassWordEditText.setText("");
		checkPasswordBtn.setEnabled(true);
	}

	private void onCheckPasswordSuccess() {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.change_tel_input_pwd_success, Toast.LENGTH_SHORT).show();
		mPassWordEditText.setEnabled(true);
		mPassWordEditText.setText("");
		checkPasswordBtn.setEnabled(true);
		gotoChangeTelStep2Activity(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_tel_step1);
		ButterKnife.inject(this);
		instance = this;
		getSupportActionBar().setTitle(R.string.modified_tel);
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
		mPassWordError.setVisibility(View.GONE);
	}
}
