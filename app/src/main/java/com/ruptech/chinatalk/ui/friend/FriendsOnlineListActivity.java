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
import com.ruptech.chinatalk.task.impl.RetrieveOnlineUserTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class FriendsOnlineListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	private final String TAG = Utils.CATEGORY
			+ FriendsOnlineListActivity.class.getSimpleName();
	@InjectView(R.id.activity_friends_request_listView)
	ListView newFriendListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendsOnlineListArrayAdapter;

	private static GenericTask mRetrieveOnlineUserTask;

	private ProgressDialog progressDialog;

	private final TaskListener mRetrieveOnlineUserTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveOnlineUserTask onlineUserTask = (RetrieveOnlineUserTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<User> onlineUserList = onlineUserTask.getOnlineUserList();
				if (onlineUserList.size() == 0) {
					notMoreDataFound = true;
					if (friendsOnlineListArrayAdapter.getCount() == 0) {
						emptyTextView.setVisibility(View.VISIBLE);
						emptyTextView.setText(R.string.friend_online_no_data);
					} else {
						emptyTextView.setVisibility(View.GONE);
					}
				} else {
					friendsOnlineListArrayAdapter.clear();
					for (User item : onlineUserList) {
						friendsOnlineListArrayAdapter.add(item);
					}
					friendsOnlineListArrayAdapter.notifyDataSetChanged();
					newFriendListView.setSelection(0);
				}
			} else {
				Toast.makeText(FriendsOnlineListActivity.this,
						onlineUserTask.getMsg(),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}

	};

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private boolean notMoreDataFound = false;

	public void doOnlineFriends() {
		if (mRetrieveOnlineUserTask != null
				&& mRetrieveOnlineUserTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRetrieveOnlineUserTask = new RetrieveOnlineUserTask();
		mRetrieveOnlineUserTask.setListener(mRetrieveOnlineUserTaskListener);

		mRetrieveOnlineUserTask.execute();
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
		getSupportActionBar().setTitle(R.string.friend_online);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// 加载画面
		setupComponents();

		doOnlineFriends();
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
			doOnlineFriends();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		friendsOnlineListArrayAdapter.notifyDataSetChanged();
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		emptyTextView.setVisibility(View.GONE);

		friendsOnlineListArrayAdapter = new FriendListArrayBaseAdapter(
				this, FriendListArrayBaseAdapter.EXTRA_FRIEND_ONLINE_LIST,
				progressDialog);
		newFriendListView.setAdapter(friendsOnlineListArrayAdapter);
		newFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (BuildConfig.DEBUG)
					Log.v(TAG, "newFriendListView onItemClick:" + position);
				User user = friendsOnlineListArrayAdapter.getItem(position);
				gotoProfileActivity(FriendsOnlineListActivity.this, user);
			}
		});

	}

}