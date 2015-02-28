package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
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
import com.ruptech.chinatalk.task.impl.UserProfileChangeTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;

public class ChangeGenderActivity extends AbstractUserActivity {

	private final String TAG = Utils.CATEGORY
			+ ChangeGenderActivity.class.getSimpleName();

	@InjectView(R.id.gender_male_radio_btn)
	RadioButton maleRadioButton;
	@InjectView(R.id.gender_female_radio_btn)
	RadioButton femaleRadioButton;

	@InjectView(R.id.activity_change_profile_gender_male_layout)
	View genderMaleLayoutView;
	@InjectView(R.id.activity_change_profile_gender_female_layout)
	View genderFemaleLayoutView;
	private ProgressDialog progressDialog;

	private GenericTask changeUserProfileTask;

	private final TaskListener changeUserProfileTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserProfileChangeTask userProfileChangeTask = (UserProfileChangeTask) task;
			if (result == TaskResult.OK) {
				User user = userProfileChangeTask.getUser();
				onUserChangeGenderSuccess(user);
			} else {
				String msg = task.getMsg();
				onUserChangeGenderFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onUserChangeGenderBegin();
		}

	};

	@OnClick(R.id.activity_change_profile_gender_female_layout)
	public void changeUserGenderToFemale(View v) {
		if (AppPreferences.USERS_GENDER_FEMALE != mUser.getGender()) {
			doChangeUserGenderTask(AppPreferences.USERS_GENDER_FEMALE);
			displayGenderFemale();
		}
	}

	@OnClick(R.id.activity_change_profile_gender_male_layout)
	public void changeUserGenderToMale(View v) {
		if (AppPreferences.USERS_GENDER_MALE != mUser.getGender()) {
			doChangeUserGenderTask(AppPreferences.USERS_GENDER_MALE);
			displayGenderMale();
		}
	}

	private void disableEntry() {
		genderMaleLayoutView.setClickable(false);
		genderFemaleLayoutView.setClickable(false);
	}

	private void displayGenderFemale() {
		maleRadioButton.setChecked(false);
		maleRadioButton.setFocusable(false);
		femaleRadioButton.setChecked(true);
		femaleRadioButton.setFocusable(true);
	}

	private void displayGenderMale() {
		maleRadioButton.setChecked(true);
		maleRadioButton.setFocusable(true);
		femaleRadioButton.setChecked(false);
		femaleRadioButton.setFocusable(false);
	}

	private void doChangeUserGenderTask(int gender) {
		changeUserProfileTask = new UserProfileChangeTask("change_column",
				"gender", String.valueOf(gender));
		changeUserProfileTask.setListener(changeUserProfileTaskListener);
		changeUserProfileTask.execute();
	}

	private void enableEntry() {
		genderMaleLayoutView.setClickable(true);
		genderFemaleLayoutView.setClickable(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_profile_gender);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.user_setting_gender);
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

	private void onUserChangeGenderBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
		disableEntry();
	}

	private void onUserChangeGenderFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
	}

	private void onUserChangeGenderSuccess(User user) {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT)
		.show();
		enableEntry();
		mUser = user;

		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	private void refleshDisplaySettingGender() {
		if (AppPreferences.USERS_GENDER_MALE == mUser.getGender()) {
			displayGenderMale();
		} else if (AppPreferences.USERS_GENDER_FEMALE == mUser.getGender()) {
			displayGenderFemale();
		} else {
			maleRadioButton.setChecked(false);
			maleRadioButton.setFocusable(false);
			femaleRadioButton.setChecked(false);
			femaleRadioButton.setFocusable(false);
		}
	}

	private void setupComponents() {
		refleshDisplaySettingGender();
	}
}
