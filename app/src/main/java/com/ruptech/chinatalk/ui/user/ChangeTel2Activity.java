package com.ruptech.chinatalk.ui.user;

import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserSignupCheckTask;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.CustomDialog;

/**
 * Activity which displays a SmsVerify screen to the user, offering registration
 * as well.
 */
public class ChangeTel2Activity extends ActionBarActivity {
	protected static final String EXTRA_MOBILE = "MOBILE";

	private static ChangeTel2Activity instance;

	protected static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private ProgressDialog progressDialog;

	// Tasks.
	private GenericTask userSignupCheckTask;

	private final TaskListener mUserSignupCheckTaskListener = new TaskAdapter() {


		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserSignupCheckTask userSignupCheckTask = (UserSignupCheckTask) task;
			if (result == TaskResult.OK) {
				User user = userSignupCheckTask.getUser();
				onRetrieveUserSuccess(user);
			} else {
				String msg = task.getMsg();
				onRetrieveUserFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onRetrieveUserBegin();
		}

	};

	private final String TAG = Utils.CATEGORY
			+ ChangeTel2Activity.class.getSimpleName();

	@InjectView(R.id.activity_change_tel_step2_tel)
	EditText mtelEdit;

	@InjectView(R.id.activity_change_tel_step2_error)
	TextView textError;

	@InjectView(R.id.activity_change_tel_step2_next_button)
	View nextButton;

	private String mTel;

	InputMethodManager mInputMethodManager;

	private void disableEntry() {
		nextButton.setEnabled(false);
		mtelEdit.setEnabled(false);
		textError.setVisibility(View.GONE);
	}

	@OnClick(R.id.activity_change_tel_step2_next_button)
	public void doVerifyMobile(View v) {
		mTel = mtelEdit.getText().toString();
		mTel = mTel.replace(" ", "");
		mTel = mTel.toLowerCase(Locale.getDefault());

		if (Utils.isEmpty(mTel)) {
			textError.setVisibility(View.VISIBLE);
			textError.setText(R.string.input_has_blank);
		} else if (!Utils.isMail(mTel) && !Utils.isTelphone(mTel)) {
			textError.setVisibility(View.VISIBLE);
			textError.setText(R.string.input_a_valid_telphone_number);
		} else if (App.readUser().getFullname().indexOf(mTel) != -1) {
			textError.setVisibility(View.VISIBLE);
			textError.setText(R.string.tel_should_not_public);
		} else {
			if (userSignupCheckTask != null
					&& userSignupCheckTask.getStatus() == GenericTask.Status.RUNNING) {
				return;
			}
			userSignupCheckTask = new UserSignupCheckTask(mTel, false);
			userSignupCheckTask.setListener(mUserSignupCheckTaskListener);
			userSignupCheckTask.execute();
		}
	}

	private void enableEntry() {
		nextButton.setEnabled(true);
		mtelEdit.setEnabled(true);
		mtelEdit.setText("");
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		instance = this;
		setContentView(R.layout.change_tel_step2);
		ButterKnife.inject(this);
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

	private void onRetrieveUserBegin() {
		progressDialog = Utils.showDialog(this,
				getString(R.string.verify_processing));
		disableEntry();
	}

	private void onRetrieveUserFailure(String msg) {
		Utils.dismissDialog(progressDialog);

		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
	}

	private void onRetrieveUserSuccess(User user) {
		Utils.dismissDialog(progressDialog);
		enableEntry();
		if (null != user) {
			new CustomDialog(this)
					.setMessage(
							getString(R.string.already_exists_user_pls_change_tel))
					.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
						}
					}).show();
		} else {
			Intent intent = new Intent(this, ChangeTel3Activity.class);
			intent.putExtra(ChangeTel2Activity.EXTRA_MOBILE, mTel);
			startActivity(intent);

		}
	}

	private void setupComponents() {
		mInputMethodManager = (InputMethodManager) this.getApplicationContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		textError.setVisibility(View.GONE);

	}
}
