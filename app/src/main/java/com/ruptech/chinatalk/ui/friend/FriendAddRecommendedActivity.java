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
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.ruptech.chinatalk.BuildConfig;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.impl.RecommendedFriendTask;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.PrefUtils;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.FriendListArrayBaseAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

public class FriendAddRecommendedActivity extends ActionBarActivity {
	private final static String TAG = Utils.CATEGORY
			+ FriendAddRecommendedActivity.class.getSimpleName();

	public static void doRecommendedFriends() {
		if (mRecommendedFriendTask != null
				&& mRecommendedFriendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}

		mRecommendedFriendTask = new RecommendedFriendTask();

		mRecommendedFriendTask.execute();
	}
	private static void gotoProfileActivity(Activity context, User user) {
		Intent intent = new Intent(context, FriendProfileActivity.class);
		intent.putExtra(ProfileActivity.EXTRA_USER, user);
		context.startActivity(intent);
	}

	@InjectView(R.id.activity_friends_request_listView)
	ListView newFriendListView;
	@InjectView(R.id.activity_friends_request_list_emptyview_text)
	TextView emptyTextView;
	private FriendListArrayBaseAdapter friendListArrayBaseAdapter;

	private static GenericTask mRecommendedFriendTask;

	private ProgressDialog progressDialog;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends_request_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.friend_recommended);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// 加载画面
		setupComponents();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		friendListArrayBaseAdapter.notifyDataSetChanged();
	}

	private void setupComponents() {
		swypeLayout.setEnabled(false);
		newFriendListView.setEmptyView(emptyTextView);

		friendListArrayBaseAdapter = new FriendListArrayBaseAdapter(
				this,
				FriendListArrayBaseAdapter.EXTRA_FRIEND_ADD_RECOMMENDED,
				progressDialog);
		newFriendListView.setAdapter(friendListArrayBaseAdapter);

		List<User> newFriendUserList = PrefUtils
				.readRecommendedFriendUserList();
		if (newFriendUserList != null && newFriendUserList.size() > 0) {
			friendListArrayBaseAdapter.clear();

			for (User item : newFriendUserList) {
				friendListArrayBaseAdapter.add(item);
			}
			friendListArrayBaseAdapter.notifyDataSetChanged();

			newFriendListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (BuildConfig.DEBUG)
						Log.v(TAG, "newFriendListView onItemClick:" + position);
					User user = friendListArrayBaseAdapter
							.getItem(position);
					gotoProfileActivity(FriendAddRecommendedActivity.this, user);
				}
			});
		}

	}

}
