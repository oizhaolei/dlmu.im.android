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

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveLbsUserTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class FriendsLbsListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {


	private final String TAG = Utils.CATEGORY
			+ FriendsLbsListActivity.class.getSimpleName();
	@InjectView(R.id.activity_friends_request_listView)
	ListView newFriendListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendsLbsListArrayAdapter;

	private static GenericTask mRetrieveLbsUserTask;

	private ProgressDialog progressDialog;

	private final TaskListener mRetrieveLbsUserTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveLbsUserTask lbsUserTask = (RetrieveLbsUserTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<User> lbsUserList = lbsUserTask.getLbsUserList();
				if (lbsUserList.size() == 0) {
					notMoreDataFound = true;
					if (friendsLbsListArrayAdapter.getCount() == 0) {
						emptyTextView.setVisibility(View.VISIBLE);
						emptyTextView.setText(R.string.friend_lbs_no_data);
					} else {
						emptyTextView.setVisibility(View.GONE);
					}
				} else {
					friendsLbsListArrayAdapter.clear();
					for (User item : lbsUserList) {
						friendsLbsListArrayAdapter.add(item);
					}
					friendsLbsListArrayAdapter.notifyDataSetChanged();
					newFriendListView.setSelection(0);
				}
			} else {
				Toast.makeText(FriendsLbsListActivity.this,
						lbsUserTask.getMsg(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			swypeLayout.setRefreshing(true);
		}

	};

	private boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	public void doLbsFriends() {
		int late6 = 0;
		int lnge6 = 0;
		if (MyLocation.recentLocation != null) {
			late6 = (Double
					.valueOf(MyLocation.recentLocation.getLatitude() * 1E6))
					.intValue();
			lnge6 = (Double
					.valueOf(MyLocation.recentLocation.getLongitude() * 1E6))
					.intValue();

			if (mRetrieveLbsUserTask != null
					&& mRetrieveLbsUserTask.getStatus() == GenericTask.Status.RUNNING) {
				return;
			}
			mRetrieveLbsUserTask = new RetrieveLbsUserTask(late6, lnge6);
			mRetrieveLbsUserTask.setListener(mRetrieveLbsUserTaskListener);
			mRetrieveLbsUserTask.execute();
		}
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
		getSupportActionBar().setTitle(R.string.friend_lbs);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// 加载画面
		setupComponents();

		doLbsFriends();
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
			doLbsFriends();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		friendsLbsListArrayAdapter.notifyDataSetChanged();
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		emptyTextView.setVisibility(View.GONE);

		friendsLbsListArrayAdapter = new FriendListArrayBaseAdapter(this,
				FriendListArrayBaseAdapter.EXTRA_FRIEND_LBS_LIST,
				progressDialog);
		newFriendListView.setAdapter(friendsLbsListArrayAdapter);
		newFriendListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (BuildConfig.DEBUG)
					Log.v(TAG, "newFriendListView onItemClick:" + position);
				User user = friendsLbsListArrayAdapter.getItem(position);
				gotoProfileActivity(FriendsLbsListActivity.this, user);
			}
		});

	}

}