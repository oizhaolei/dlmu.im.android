package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangeFullNameActivity extends AbstractUserActivity {

	private final String TAG = Utils.CATEGORY
			+ ChangeFullNameActivity.class.getSimpleName();

	@InjectView(R.id.activity_change_profile_fullname_text)
	EditText mfullnameTextView;

	@InjectView(R.id.activity_change_profile_fullname_error_text)
	TextView fullnameTextError;
	private GenericTask mUserProfileChangeTask;
	private ProgressDialog progressDialog;
	private final TaskListener mUserProfileChangeTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserProfileChangeTask userProfileChangeTask = (UserProfileChangeTask) task;
			if (result == TaskResult.OK) {
				User user = userProfileChangeTask.getUser();
				onFullNameChangeSuccess(user);
			} else {
				String msg = task.getMsg();
				onFullNameChangeFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onFullNameChangeBegin();
		}
	};

	private MenuItem saveMenu;

	private void disableEntry() {
		mfullnameTextView.setEnabled(false);
		saveMenu.setEnabled(false);
		fullnameTextError.setVisibility(View.GONE);
	}

	private void enableEntry() {
		mfullnameTextView.setEnabled(true);
		saveMenu.setEnabled(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (App.readUser() == null) {
			finish();
		}
		setContentView(R.layout.change_profile_fullname);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.change_fullname);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setupComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		int order = 0;

		saveMenu = menu.add(Menu.NONE, Menu.FIRST + order, order++,
				R.string.save);

		saveMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return super.onCreateOptionsMenu(menu);
	}

	private void onFullNameChangeBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
		disableEntry();
	}

	private void onFullNameChangeFailure(String msg) {
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		Utils.dismissDialog(progressDialog);
		enableEntry();
		this.finish();
	}

	private void onFullNameChangeSuccess(User user) {
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
		enableEntry();
		Utils.dismissDialog(progressDialog);
		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == saveMenu.getItemId()) {
			saveFullname();
		}
		return true;
	}

	private void saveFullname() {
		String fullname = mfullnameTextView.getText().toString().trim();
		if (Utils.isEmpty(fullname)) {
			fullnameTextError.setVisibility(View.VISIBLE);
			return;
		}else if (fullname.indexOf(App.readUser().getTel()) != -1) {
			Toast.makeText(this, R.string.tel_should_not_public, Toast.LENGTH_LONG).show();
			return;
		}
		mUserProfileChangeTask = new UserProfileChangeTask("change_column",
				"fullname", fullname);
		mUserProfileChangeTask.setListener(mUserProfileChangeTaskListener);
		mUserProfileChangeTask.execute();
	}

	private void setupComponents() {
		mfullnameTextView.setText(mUser.getFullname());
		fullnameTextError.setVisibility(View.GONE);
	}
}
