package com.ruptech.chinatalk.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.UserMemoChangeTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;

public class ChangeFriendMemoActivity extends AbstractUserActivity {

	private final String TAG = Utils.CATEGORY
			+ ChangeFriendMemoActivity.class.getSimpleName();

	@InjectView(R.id.activity_change_friend_memo_text)
	EditText mMemoTextView;

	private GenericTask memoChangeTask;

	private Friend mFriend;

	private final TaskListener memoChangeListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			UserMemoChangeTask memochangetask = (UserMemoChangeTask) task;
			if (result == TaskResult.OK) {
				Friend friend = memochangetask.getFriend();
				onMemoChangeSuccess(friend);
			} else {
				String msg = task.getMsg();
				onMemoChangeFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			onMemoChangeBegin();
		}
	};

	private MenuItem saveMore;

	private ProgressDialog progressDialog;

	public void btn_cancel(View v) {
		this.finish();
	}

	private void disableEntry() {
		mMemoTextView.setEnabled(false);
		saveMore.setEnabled(false);
	}

	private void enableEntry() {
		mMemoTextView.setEnabled(true);
		saveMore.setEnabled(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (App.readUser() == null) {
			finish();
			return;
		}

		setContentView(R.layout.change_friend_memo);
		ButterKnife.inject(this);
		mFriend = App.friendDAO.fetchFriend(App.readUser().getId(),
				mUser.getId());
		getSupportActionBar().setTitle(R.string.change_memo);
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

	private void onMemoChangeBegin() {
		progressDialog = Utils.showDialog(this, getString(R.string.data_saving));
		disableEntry();
	}
	private void onMemoChangeFailure(String msg) {
		Utils.dismissDialog(progressDialog);
		if (!Utils.isEmpty(msg)) {
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		enableEntry();
		this.finish();
	}

	private void onMemoChangeSuccess(Friend friend) {
		Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show();
		enableEntry();
		Utils.dismissDialog(progressDialog);
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
			saveMemo();
		}
		return true;
	}

	private void saveMemo() {
		String memo = mMemoTextView.getText().toString();
		if (Utils.isEmpty(memo)) {
			memo = "";
		}

		memoChangeTask = new UserMemoChangeTask(mUser.getId(), memo);
		memoChangeTask.setListener(memoChangeListener);
		memoChangeTask.execute();
	}

	private void setupComponents() {
		if (Utils.isEmpty(mFriend.getFriend_memo())) {
			mMemoTextView.setText("");
		} else {
			mMemoTextView.setText(mFriend.getFriend_memo());
		}
	}
}
