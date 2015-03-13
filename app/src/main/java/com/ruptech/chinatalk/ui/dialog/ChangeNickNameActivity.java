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
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.FriendNickNameChangeTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChangeNickNameActivity extends AbstractUserActivity {

	private final String TAG = Utils.CATEGORY
			+ ChangeNickNameActivity.class.getSimpleName();

	@InjectView(R.id.activity_change_profile_nickname_text)
	EditText mNicknameTextView;
	@InjectView(R.id.activity_change_profile_nickname_error_text)
	TextView nicknameTextError;
	private GenericTask mNickNameChangeTask;

	private Friend mFriend;
	private ProgressDialog progressDialog;

	private final TaskListener mNickNameChangeListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			FriendNickNameChangeTask nickNameTask = (FriendNickNameChangeTask) task;
			if (result == TaskResult.OK) {
				Friend friend = nickNameTask.getFriend();
				onNickNameChangeSuccess(friend);
			} else {
				String msg = task.getMsg();
				onNickNameChangeFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onNickNameChangeBegin();
		}

	};

	private MenuItem saveMore;

	private void disableEntry() {
		nicknameTextError.setVisibility(View.GONE);
		mNicknameTextView.setEnabled(false);
		saveMore.setEnabled(false);
	}

	private void enableEntry() {
		mNicknameTextView.setEnabled(true);
		saveMore.setEnabled(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}
		setContentView(R.layout.change_profile_nickname);
		ButterKnife.inject(this);
		mFriend = App.friendDAO.fetchFriend(App.readUser().getId(), mUser.getId());

		getSupportActionBar().setTitle(R.string.change_nickname);
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

	private void onNickNameChangeBegin() {
		progressDialog = Utils
				.showDialog(this, getString(R.string.data_saving));
		disableEntry();
	}

	private void onNickNameChangeFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
		this.finish();
	}

	private void onNickNameChangeSuccess(Friend friend) {
		Utils.dismissDialog(progressDialog);
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
		enableEntry();

		Intent intent = getIntent();
		intent.putExtra(ProfileActivity.EXTRA_FRIEND, friend);
		setResult(Activity.RESULT_OK, intent);

		this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		} else if (item.getItemId() == saveMore.getItemId()) {
			saveNickname();
		}
		return true;
	}

	private void saveNickname() {
		String nickname = mNicknameTextView.getText().toString().trim();
		if (Utils.isEmpty(nickname)) {
			nicknameTextError.setVisibility(View.VISIBLE);
			return;
		} else if (!Utils.isEmpty(mUser.getTel()) && nickname.indexOf(mUser.getTel()) != -1) {
			Toast.makeText(this, R.string.tel_should_not_public, Toast.LENGTH_LONG).show();
			return;
		}

		mNickNameChangeTask = new FriendNickNameChangeTask(nickname,
				mUser.getId());
		mNickNameChangeTask.setListener(mNickNameChangeListener);
		mNickNameChangeTask.execute();
	}

	private void setupComponents() {
		if (!Utils.isEmpty(mFriend.getFriend_nickname())) {
			mNicknameTextView.setText(mFriend.getFriend_nickname());
		}
		nicknameTextError.setVisibility(View.GONE);
	}
}
