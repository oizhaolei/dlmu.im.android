package com.ruptech.chinatalk.ui.story;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveStoryLikeListTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

public class UserStoryLikeListActivity extends ActionBarActivity implements
		OnRefreshListener {

	protected static final String EXTRA_PHOTO_ID = "EXTRA_PHOTO_ID";
	private long mPhotoID;
	private final String TAG = Utils.CATEGORY
			+ UserStoryLikeListActivity.class.getSimpleName();
	@InjectView(R.id.activity_friends_request_listView)
	ListView mStoryLikeListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendsFollowerListArrayAdapter;
	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;
	private GenericTask mRetrieveStoryLikeListTask;
	private boolean notMoreUpDataFound = false;
	private boolean notMoreDownDataFound = false;
	private static UserStoryLikeListActivity instance = null;

	private final TaskListener mStoryLikeListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveStoryLikeListTask retrieveLikeUserListTask = (RetrieveStoryLikeListTask) task;

			if (result == TaskResult.OK) {
				List<User> mLikeUserList = retrieveLikeUserListTask
						.getLikeUserList();

				if (mLikeUserList.size() == 0) {
					Toast.makeText(UserStoryLikeListActivity.this,
							R.string.no_new_data,
							Toast.LENGTH_SHORT).show();
				}

				if (mLikeUserList.size() < AppPreferences.PAGE_COUNT_20) {
					if (retrieveLikeUserListTask.isTop())
						notMoreUpDataFound = true;
					else
						notMoreDownDataFound = true;
				} else {
					if (retrieveLikeUserListTask.isTop())
						notMoreUpDataFound = false;
					else
						notMoreDownDataFound = false;
				}

				boolean isUp = retrieveLikeUserListTask.isTop();
				addToAdapter(mLikeUserList, isUp);
				if (friendsFollowerListArrayAdapter.getCount() == 0) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(R.string.no_like_count);
				} else {
					emptyTextView.setVisibility(View.GONE);
				}

				onRetrieveLikeUserListSuccess();
			} else {
				String msg = task.getMsg();
				onRetrieveLikeUserListFailure(msg);
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveStoryLikeListTask retrieveLikeUserListTask = (RetrieveStoryLikeListTask) task;

			onRetrieveLikeUserListBegin(retrieveLikeUserListTask.isTop());
		}
	};

	private ProgressDialog progressDialog;

	private void addToAdapter(List<User> mUserList, boolean up) {
		int pos = 0;
		for (User user : mUserList) {
			// request auto translate
			if (up) {
				friendsFollowerListArrayAdapter.insert(user, pos++);
			} else {
				friendsFollowerListArrayAdapter.add(user);
			}
		}
	}

	private void doRetrieveStoryLikeList(boolean top) {
		if ((top && notMoreUpDataFound)
				|| (!top && notMoreDownDataFound)
				|| (mRetrieveStoryLikeListTask != null && mRetrieveStoryLikeListTask
						.getStatus() == GenericTask.Status.RUNNING)) {
			if (top && notMoreUpDataFound) {
				Toast.makeText(UserStoryLikeListActivity.this,
						R.string.no_new_data,
						Toast.LENGTH_SHORT).show();
			} else if (!top && notMoreDownDataFound) {
				Toast.makeText(UserStoryLikeListActivity.this,
						R.string.no_more_data,
						Toast.LENGTH_SHORT).show();
			}
			swypeLayout.setRefreshing(false);
			return;
		}

		long sinceId;
		long maxId;
		if (top) {
			sinceId = getSinceId();
			maxId = AppPreferences.ID_IMPOSSIBLE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		mRetrieveStoryLikeListTask = new RetrieveStoryLikeListTask(top,
				mPhotoID, maxId, sinceId);
		mRetrieveStoryLikeListTask.setListener(mStoryLikeListTaskListener);
		mRetrieveStoryLikeListTask.execute();
	}

	private long getMaxId() {
		if (friendsFollowerListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return friendsFollowerListArrayAdapter.getItem(
					friendsFollowerListArrayAdapter.getCount() - 1).getLikeId() - 1;
		}
	}

	private long getSinceId() {
		if (friendsFollowerListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return friendsFollowerListArrayAdapter.getItem(0).getLikeId() + 1;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Yellow_light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_request_list);
		instance = this;
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.favorite);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		parseExtras();
		setupComponents();
		doRetrieveStoryLikeList(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onRefresh(boolean isUp) {
		swypeLayout.setProgressTop(isUp);
		doRetrieveStoryLikeList(isUp);
	}

	private void onRetrieveLikeUserListBegin(boolean isUp) {
		swypeLayout.setProgressTop(isUp);
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveLikeUserListFailure(String msg) {
		swypeLayout.setRefreshing(false);
		Toast.makeText(UserStoryLikeListActivity.this, msg, Toast.LENGTH_SHORT)
				.show();
	}

	private void onRetrieveLikeUserListSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void parseExtras() {
		Bundle extras = getIntent().getExtras();
		mPhotoID = extras.getLong(EXTRA_PHOTO_ID, -1);
	}
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		emptyTextView.setVisibility(View.GONE);
		emptyTextView.setText("");
		mStoryLikeListView.setEmptyView(emptyTextView);

		friendsFollowerListArrayAdapter = new FriendListArrayBaseAdapter(
				this,
				FriendListArrayBaseAdapter.EXTRA_FRIEND_LIKE_STORY_LIST,
				progressDialog);
		mStoryLikeListView.setAdapter(friendsFollowerListArrayAdapter);
		mStoryLikeListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position >= 0) {
					User user = (User) parent.getItemAtPosition(position);

					Intent intent = new Intent(UserStoryLikeListActivity.this,
							FriendProfileActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER, user);
					UserStoryLikeListActivity.this.startActivity(intent);
				}
			}

		});
	}
}