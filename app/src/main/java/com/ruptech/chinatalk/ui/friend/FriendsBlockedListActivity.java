package com.ruptech.chinatalk.ui.friend;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Friend;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveBlockedFriendsTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class FriendsBlockedListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	private final String TAG = Utils.CATEGORY
			+ FriendsBlockedListActivity.class.getSimpleName();
	@InjectView(R.id.activity_friends_request_listView)
	ListView blockedFriendListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendsBlockedListArrayAdapter;

	private static GenericTask mRetrieveBlockFriendsTask;


	private ProgressDialog progressDialog;

	private boolean notMoreDataFound = false;

	private final TaskListener mRetrieveBlockedFriendsTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveBlockedFriendsTask blockFriendListTask = (RetrieveBlockedFriendsTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<User> blockedFriendsList = blockFriendListTask
						.getBlockedFriendsList();
				sinceId = blockFriendListTask.getSinceId();
				if (blockedFriendsList != null && blockedFriendsList.size() == 0) {
					notMoreDataFound = true;
					if (friendsBlockedListArrayAdapter.getCount() == 0) {
						emptyTextView.setVisibility(View.VISIBLE);
						emptyTextView.setText(R.string.no_data_found);
					} else {
						emptyTextView.setVisibility(View.GONE);
					}
				}
				for (User item : blockedFriendsList) {
					friendsBlockedListArrayAdapter.add(item);
				}
				friendsBlockedListArrayAdapter.notifyDataSetChanged();
			} else {
				Toast.makeText(FriendsBlockedListActivity.this,
						blockFriendListTask.getMsg(), Toast.LENGTH_SHORT)
						.show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}

	};

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private long sinceId = 0;

	public void doRetrieveBlockedFriends() {
		if (mRetrieveBlockFriendsTask != null
				&& mRetrieveBlockFriendsTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveBlockFriendsTask = new RetrieveBlockedFriendsTask(sinceId);
		mRetrieveBlockFriendsTask
				.setListener(mRetrieveBlockedFriendsTaskListener);
		mRetrieveBlockFriendsTask.execute();
	}

	private void gotoProfileActivity(Activity context, User user) {
		Intent intent = new Intent(context, FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_request_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.block_users);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// 加载画面
		setupComponents();

		doRetrieveBlockedFriends();
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
		swypeLayout.setRefreshing(false);
		if (notMoreDataFound) {
			Toast.makeText(this, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
		} else if (!isUp) {
			doRetrieveBlockedFriends();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		for (int i = 0; i < friendsBlockedListArrayAdapter.getCount(); i++) {
			User friendUser = friendsBlockedListArrayAdapter.getItem(i);
			Friend userFriendInfo = App.friendDAO.fetchFriend(App.readUser()
					.getId(), friendUser.getId());
			if (userFriendInfo != null && userFriendInfo.getDone() == 1) {
				friendsBlockedListArrayAdapter.remove(friendUser);
			}
		}
		friendsBlockedListArrayAdapter.notifyDataSetChanged();
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		emptyTextView.setText(getString(R.string.no_found_block_users));
		emptyTextView.setVisibility(View.GONE);

		friendsBlockedListArrayAdapter = new FriendListArrayBaseAdapter(
				this, FriendListArrayBaseAdapter.EXTRA_FRIEND_BLOCKED_LIST,
				progressDialog);
		blockedFriendListView.setAdapter(friendsBlockedListArrayAdapter);
		blockedFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (BuildConfig.DEBUG)
					Log.v(TAG, "newFriendListView onItemClick:" + position);
				User user = friendsBlockedListArrayAdapter.getItem(position);
				gotoProfileActivity(FriendsBlockedListActivity.this, user);
			}
		});

	}

}