package com.ruptech.chinatalk.ui;

import java.util.Locale;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FindPasswordTask;
import com.ruptech.chinatalk.utils.Utils;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class FindPasswordActivity extends ActionBarActivity {

	private final String TAG = Utils.CATEGORY
			+ FindPasswordActivity.class.getSimpleName();

	static final String PREF_USERINFO_NAME = "PREF_USERINFO_NAME";

	private ProgressDialog progressDialog;

	// Tasks.
	private GenericTask mFindPasswordTask;

	private final TaskListener mFindPasswordTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onFindPasswordSuccess();
			} else {
				String msg = task.getMsg();
				onFindPasswordFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onFindPasswordBegin();
		}

	};

	@InjectView(R.id.activity_find_password_login_button)
	View findPasswordButton;

	// UI references.
	@InjectView(R.id.activity_find_pwd_username_edittext)
	EditText mTelEditText;

	private String mTel;

	public static FindPasswordActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private void disableEntry() {
		mTelEditText.setEnabled(false);
		findPasswordButton.setEnabled(false);
	}

	@OnClick(R.id.activity_find_password_login_button)
	public void doFindPassword(View v) {

		mTel = mTelEditText.getText().toString();
		mTel = mTel.replace(" ", "");
		mTel = mTel.toLowerCase(Locale.getDefault());

		if (Utils.isEmpty(mTel)) {
			Toast.makeText(this, R.string.please_input_email_or_telphone,
					Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (Utils.isMail(mTel)) {
			doFindPasswordTask();
		} else {
			if (Utils.isTelphone(mTel)) {
				doFindPasswordTask();
			} else {
				Toast.makeText(this,
						R.string.please_right_input_email_or_telphone,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void doFindPasswordTask() {
		enableEntry();
		mFindPasswordTask = new FindPasswordTask(mTel);
		mFindPasswordTask.setListener(mFindPasswordTaskListener);
		mFindPasswordTask.execute();
	}

	private void enableEntry() {
		mTelEditText.setEnabled(true);
		findPasswordButton.setEnabled(true);
	}

	private void extras() {
		Bundle extras = getIntent().getExtras();
		mTel = extras.getString(PREF_USERINFO_NAME);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_password);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.find_password);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		instance = this;

		extras();

		setupComponents();
	}

	private void onFindPasswordBegin() {
		disableEntry();
		progressDialog = Utils.showDialog(this,
				getString(R.string.searching_password));
	}

	private void onFindPasswordFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		enableEntry();
		finish();
	}

	private void onFindPasswordSuccess() {
		Utils.dismissDialog(progressDialog);
		if (Utils.isMail(mTel)) {
			Toast.makeText(this, R.string.new_password_send_by_email,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, R.string.new_password_send_by_tel,
					Toast.LENGTH_SHORT).show();
		}

		LoginSignupActivity.setUserNameEditText(mTelEditText.getText().toString().trim());
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void setupComponents() {
		// Set up the login form.
		mTelEditText.setText(mTel);
	}
}
