package com.ruptech.chinatalk.ui.story;

import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
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

import com.ruptech.chinatalk.PresentDonateReceiver;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Gift;
import com.ruptech.chinatalk.model.User;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserGiftListTask;
import com.ruptech.chinatalk.ui.gift.GiftDonateActivity;
import com.ruptech.chinatalk.ui.user.FriendProfileActivity;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.StoryGiftListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

public class UserStoryGiftListActivity extends ActionBarActivity implements
		OnRefreshListener {
	protected long mUserPhotoId;

	protected long mUserId;

	private final String TAG = Utils.CATEGORY
			+ UserStoryGiftListActivity.class.getSimpleName();
	@InjectView(R.id.activity_story_gift_listview)
	ListView mStoryGiftListView;
	@InjectView(R.id.activity_story_gift_empty_textview)
	TextView emptyTextView;
	private StoryGiftListArrayAdapter mStoryGiftListAdapter;
	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;
	private GenericTask mRetrieveGiftListTask;
	private final TaskListener mRetrieveGiftListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserGiftListTask retrieveUserGiftTask = (RetrieveUserGiftListTask) task;
			if (result == TaskResult.OK) {
				List<Gift> giftList = retrieveUserGiftTask.getGiftList();
				if (giftList.size() == 0) {
					Toast.makeText(UserStoryGiftListActivity.this,
							R.string.no_new_data,
							Toast.LENGTH_SHORT).show();
				}

				boolean isUp = retrieveUserGiftTask.isTop();
				addToAdapter(giftList, isUp);

				if (mStoryGiftListAdapter.getCount() == 0) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(R.string.no_data_found);
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
			RetrieveUserGiftListTask retrieveUserGiftTask = (RetrieveUserGiftListTask) task;
			onRetrieveLikeUserListBegin(retrieveUserGiftTask.isTop());
		}
	};

	private void addToAdapter(List<Gift> mGiftList, boolean up) {
		int pos = 0;
		for (Gift gift : mGiftList) {
			// request auto translate
			if (up) {
				mStoryGiftListAdapter.insert(gift, pos++);
			} else {
				mStoryGiftListAdapter.add(gift);
			}
		}
	}

	private void doRetrieveGiftList(boolean top) {
		if (mRetrieveGiftListTask != null
				&& mRetrieveGiftListTask.getStatus() == GenericTask.Status.RUNNING) {
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

		mRetrieveGiftListTask = new RetrieveUserGiftListTask(top, mUserId,
				mUserPhotoId, maxId, sinceId);
		mRetrieveGiftListTask.setListener(mRetrieveGiftListTaskListener);

		mRetrieveGiftListTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private long getMaxId() {
		if (mStoryGiftListAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mStoryGiftListAdapter.getItem(
					mStoryGiftListAdapter.getCount() - 1).getId() - 1;
		}
	}

	private long getSinceId() {
		if (mStoryGiftListAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mStoryGiftListAdapter.getItem(0).getId() + 1;
		}
	}

	public User getUserFromExtras() {
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			User user = (User) extras.get(ProfileActivity.EXTRA_USER);
			return user;
		}
		return null;
	}

	public long getUserIdFromExtras() {
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			long userId = extras.getLong(ProfileActivity.EXTRA_USER_ID);
			return userId;
		}
		return 0;
	}

	public long getUserPhotoIdFromExtras() {
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			long userId = extras
					.getLong(GiftDonateActivity.EXTRA_TO_USER_PHOTO_ID);
			return userId;
		}
		return 0;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Yellow_light);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_gift_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.present);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		User mUser = getUserFromExtras();
		if (mUser == null) {
			mUserId = getUserIdFromExtras();
		} else {
			mUserId = mUser.getId();
		}

		mUserPhotoId = getUserPhotoIdFromExtras();
		setupComponents();
		doRetrieveGiftList(false);

		PresentDonateReceiver.count_of_gift_notification = 0;
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
		doRetrieveGiftList(isUp);
	}

	private void onRetrieveLikeUserListBegin(boolean isUp) {
		swypeLayout.setProgressTop(isUp);
		swypeLayout.setRefreshing(true);
	}

	private void onRetrieveLikeUserListFailure(String msg) {
		swypeLayout.setRefreshing(false);
		Toast.makeText(UserStoryGiftListActivity.this, msg, Toast.LENGTH_SHORT)
				.show();
	}

	private void onRetrieveLikeUserListSuccess() {
		swypeLayout.setRefreshing(false);
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);

		mStoryGiftListView.setEmptyView(emptyTextView);
		emptyTextView.setVisibility(View.GONE);
		emptyTextView.setText("");

		mStoryGiftListAdapter = new StoryGiftListArrayAdapter(this);
		mStoryGiftListView.setAdapter(mStoryGiftListAdapter);
		mStoryGiftListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position >= 0) {
					Gift gift = (Gift) parent.getItemAtPosition(position);

					Intent intent = new Intent(UserStoryGiftListActivity.this,
							FriendProfileActivity.class);
					intent.putExtra(ProfileActivity.EXTRA_USER_ID,
							gift.getUserid());
					UserStoryGiftListActivity.this.startActivity(intent);
				}
			}
		});
	}
}