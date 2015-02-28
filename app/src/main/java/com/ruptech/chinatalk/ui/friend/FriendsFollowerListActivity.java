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
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveFollowerUserTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class FriendsFollowerListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	private final String TAG = Utils.CATEGORY
			+ FriendsFollowerListActivity.class.getSimpleName();
	@InjectView(R.id.activity_friends_request_listView)
	ListView newFriendListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendsFollowerListArrayAdapter;

	private static GenericTask mRetrieveFollowerUserTask;

	private ProgressDialog progressDialog;

	private boolean notMoreDataFound = false;

	private final TaskListener mRetrieveFollowerUserTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveFollowerUserTask followerListTask = (RetrieveFollowerUserTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<User> followerList = followerListTask.getFollowerList();

				if (followerList != null && followerList.size() == 0) {
					notMoreDataFound = true;
					if (friendsFollowerListArrayAdapter.getCount() == 0) {
						emptyTextView.setVisibility(View.VISIBLE);
						emptyTextView.setText(R.string.follower_no_data);
					} else {
						emptyTextView.setVisibility(View.GONE);
					}
				}
				for (User item : followerList) {
					friendsFollowerListArrayAdapter.add(item);
				}
				friendsFollowerListArrayAdapter.notifyDataSetChanged();

				if (sinceId == 0) {
					App.mBadgeCount.friendCount = 0;
					CommonUtilities
							.broadcastRefreshNewMark(FriendsFollowerListActivity.this);
				}

				sinceId = followerListTask.getSinceId();
			} else {
				Toast.makeText(FriendsFollowerListActivity.this,
						followerListTask.getMsg(), Toast.LENGTH_SHORT).show();
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

	public void doFollowerFriends() {
		if (mRetrieveFollowerUserTask != null
				&& mRetrieveFollowerUserTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveFollowerUserTask = new RetrieveFollowerUserTask(sinceId);
		mRetrieveFollowerUserTask
				.setListener(mRetrieveFollowerUserTaskListener);
		mRetrieveFollowerUserTask.execute();
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
		getSupportActionBar().setTitle(R.string.friend_fans);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// 加载画面
		setupComponents();

		doFollowerFriends();
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
			doFollowerFriends();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		friendsFollowerListArrayAdapter.notifyDataSetChanged();
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		emptyTextView.setVisibility(View.GONE);

		friendsFollowerListArrayAdapter = new FriendListArrayBaseAdapter(this,
				FriendListArrayBaseAdapter.EXTRA_FRIEND_FOLLOWER_LIST,
				progressDialog);
		newFriendListView.setAdapter(friendsFollowerListArrayAdapter);
		newFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (BuildConfig.DEBUG)
					Log.v(TAG, "newFriendListView onItemClick:" + position);
				User user = friendsFollowerListArrayAdapter.getItem(position);
				gotoProfileActivity(FriendsFollowerListActivity.this, user);
			}
		});

	}

}