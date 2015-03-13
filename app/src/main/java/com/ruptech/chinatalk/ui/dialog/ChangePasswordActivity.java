package com.ruptech.chinatalk.ui.dialog;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.ChangePasswordTask;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangePasswordActivity extends ActionBarActivity {

	// Tasks.
	private GenericTask mChangePasswordTask;

	private final TaskListener mChangePasswordListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onChangePasswordSuccess();
			} else {
				onChangePasswordFailure(task.getMsg());
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onChangePasswordBegin();
		}

	};

	private final String TAG = Utils.CATEGORY
			+ ChangePasswordActivity.class.getSimpleName();

	@InjectView(R.id.activity_change_profile_old_password_text)
	EditText mOldPasswordEdit;
	@InjectView(R.id.activity_change_profile_new_password_text)
	EditText mNewPasswordEdit;
	@InjectView(R.id.activity_change_profile_repeat_password_text)
	EditText mRePasswordEdit;
	@InjectView(R.id.activity_change_profile_old_password_error_text)
	TextView oldPwdTextError;
	@InjectView(R.id.activity_change_profile_new_password_error_text)
	TextView newPwdTextError;
	@InjectView(R.id.activity_change_profile_repeat_password_error_text)
	TextView repeatPwdTextError;

	private MenuItem saveMore;
	private ProgressDialog progressDialog;

	private void disableEntry() {
		oldPwdTextError.setVisibility(View.GONE);
		newPwdTextError.setVisibility(View.GONE);
		repeatPwdTextError.setVisibility(View.GONE);
		mOldPasswordEdit.setEnabled(false);
		mNewPasswordEdit.setEnabled(false);
		mRePasswordEdit.setEnabled(false);
		saveMore.setEnabled(false);
	}

	private void enableEntry() {
		mOldPasswordEdit.setEnabled(true);
		mNewPasswordEdit.setEnabled(true);
		mRePasswordEdit.setEnabled(true);
		saveMore.setEnabled(true);
	}

	private void onChangePasswordBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
		disableEntry();
	}

	private void onChangePasswordFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
		this.finish();
	}

	private void onChangePasswordSuccess() {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.save_success,
				Toast.LENGTH_LONG).show();
		enableEntry();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}
		setContentView(R.layout.change_profile_password);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.change_password);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMore = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.save);

		saveMore.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == saveMore.getItemId()) {
			savePassword();
		}
		return true;
	}

	public void savePassword() {

		String oldPassword = mOldPasswordEdit.getText().toString();
		String password = mNewPasswordEdit.getText().toString();
		String rePassword = mRePasswordEdit.getText().toString();
		if (Utils.isEmpty(oldPassword)) {
			oldPwdTextError.setVisibility(View.VISIBLE);
			return;
		} else if (Utils.isEmpty(password)) {
			oldPwdTextError.setVisibility(View.GONE);
			newPwdTextError.setVisibility(View.VISIBLE);
			return;
		} else if (Utils.isEmpty(rePassword)) {
			oldPwdTextError.setVisibility(View.GONE);
			newPwdTextError.setVisibility(View.GONE);
			repeatPwdTextError.setVisibility(View.VISIBLE);
			repeatPwdTextError
					.setText(R.string.prompt_input_your_repeat_password_error);
			return;
		} else if (!password.equals(rePassword)) {
			oldPwdTextError.setVisibility(View.GONE);
			newPwdTextError.setVisibility(View.GONE);
			repeatPwdTextError.setVisibility(View.VISIBLE);
			repeatPwdTextError
					.setText(R.string.prompt_input_your_repeat_password_error2);
			return;
		} else {
			repeatPwdTextError.setVisibility(View.GONE);
		}

		if (mChangePasswordTask != null
				&& mChangePasswordTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mChangePasswordTask = new ChangePasswordTask(oldPassword, password);
		mChangePasswordTask.setListener(mChangePasswordListener);
		mChangePasswordTask.execute();
	}

	private void setupComponents() {
		oldPwdTextError.setVisibility(View.GONE);
		newPwdTextError.setVisibility(View.GONE);
		repeatPwdTextError.setVisibility(View.GONE);
	}
}
