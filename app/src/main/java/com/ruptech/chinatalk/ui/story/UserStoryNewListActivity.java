package com.ruptech.chinatalk.ui.story;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.map.MyLocation;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoListTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.UserStoryGridArrayAdapter;
import com.ruptech.chinatalk.widget.UserStoryListArrayAdapter;
import com.ruptech.chinatalk.widget.UserStoryListCursorAdapter;

public class UserStoryNewListActivity extends ActionBarActivity implements
		SwipeRefreshLayout.OnRefreshListener {

	public static final String EXTRA_USER_STORY_TYPE = "EXTRA_USER_STORY_TYPE";

	public static final String STORY_TYPE_LBS = "lbs";
	public static final String STORY_TYPE_NEW = "new";

	MenuItem menuItemChangeType;

	SwipeRefreshLayout swypeLayout;
	TextView emptyTextView;
	ListView userStoryListView;
	GridView userStoryGridView;
	private static GenericTask mRetrieveUserPhotoListTask;
	protected UserStoryListArrayAdapter mUserStoryListArrayAdapter;
	protected UserStoryGridArrayAdapter mUserStoryGridArrayAdapter;

	private final TaskListener mRetrieveUserPhotoListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveUserPhotoListTask popularStoryTask = (RetrieveUserPhotoListTask) task;
			swypeLayout.setRefreshing(false);
			if (result == TaskResult.OK) {
				List<UserPhoto> mUserPhotoList = popularStoryTask
						.getUserPhotoList();
				if (!popularStoryTask.isTop()
						&& mUserPhotoList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}

				addAllStoryArrayAdapter(mUserPhotoList,
						popularStoryTask.isTop());
				mAllUserPhotoList.addAll(mUserPhotoList);
				if (mAllUserPhotoList.size() == 0) {
					emptyTextView.setVisibility(View.VISIBLE);
					emptyTextView.setText(R.string.no_data_found);
				} else {
					emptyTextView.setVisibility(View.GONE);
				}
			} else {
				Toast.makeText(UserStoryNewListActivity.this,
						popularStoryTask.getMsg(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveUserPhotoListTask popularStoryTask = (RetrieveUserPhotoListTask) task;
			swypeLayout.setRefreshing(popularStoryTask.isTop());
		}
	};

	private boolean notMoreDataFound = false;

	private boolean isListlayout = false;
	private String storyType;
	List<UserPhoto> mAllUserPhotoList;

	protected final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			UserPhoto story = (UserPhoto) intent.getExtras().getSerializable(
					CommonUtilities.EXTRA_MESSAGE);
			if (story != null && mUserStoryListArrayAdapter != null) {
				mUserStoryListArrayAdapter.changeUserPhoto(story);
			}
		}
	};

	private void addAllStoryArrayAdapter(List<UserPhoto> mUserPhotoList,
			boolean up) {
		int pos = 0;
		for (UserPhoto userPhoto : mUserPhotoList) {
			if (up) {
				if (isListlayout) {
					mUserStoryListArrayAdapter.insert(userPhoto, pos++);
				} else {
					mUserStoryGridArrayAdapter.insert(userPhoto, pos++);
				}
			} else {
				if (isListlayout) {
					mUserStoryListArrayAdapter.add(userPhoto);
				} else {
					mUserStoryGridArrayAdapter.add(userPhoto);
				}
			}
		}
	}

	public void doChangeType(MenuItem item) {
		if (isListlayout) {
			isListlayout = false;
			setContentView(R.layout.activity_story_new_grid);
			setupGridComponents();
			menuItemChangeType.setIcon(R.drawable.ic_menu_as_list);
		} else {
			isListlayout = true;
			setContentView(R.layout.activity_story_new_list);
			setupComponents();
			menuItemChangeType.setIcon(R.drawable.ic_menu_as_grid);
		}
		addAllStoryArrayAdapter(mAllUserPhotoList, true);
	}

	public void doRetrieveUserPhotoList(boolean top) {
		if (mRetrieveUserPhotoListTask != null
				&& mRetrieveUserPhotoListTask.getStatus() == GenericTask.Status.RUNNING) {
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
		if (storyType.equals(STORY_TYPE_NEW)) {
			mRetrieveUserPhotoListTask = new RetrieveUserPhotoListTask(top,
					maxId, sinceId, null);
		} else if (storyType.equals(STORY_TYPE_LBS)) {
			int late6 = 0;
			int lnge6 = 0;
			if (MyLocation.recentLocation != null) {
				late6 = (Double
						.valueOf(MyLocation.recentLocation.getLatitude() * 1E6))
						.intValue();
				lnge6 = (Double.valueOf(MyLocation.recentLocation
						.getLongitude() * 1E6)).intValue();

				mRetrieveUserPhotoListTask = new RetrieveUserPhotoListTask(top,
						maxId, sinceId, -1, 0,
						AbstractUserStoryListActivity.STORY_TYPE_LOCATION,
						late6, lnge6, "", "");

			} else {
				return;
			}
		} else {
			return;
		}
		mRetrieveUserPhotoListTask
				.setListener(mRetrieveUserPhotoListTaskListener);

		mRetrieveUserPhotoListTask.execute();
	}

	public long getMaxId() {
		long maxId = 0;
		if (isListlayout && mUserStoryListArrayAdapter != null
				&& mUserStoryListArrayAdapter.getCount() > 0) {
			UserPhoto userPhoto = mUserStoryListArrayAdapter
					.getItem(mUserStoryListArrayAdapter.getCount() - 1);
			maxId = userPhoto.getTimeline_id() - 1;
		} else if (!isListlayout && mUserStoryGridArrayAdapter != null
				&& mUserStoryGridArrayAdapter.getCount() > 0) {
			UserPhoto userPhoto = mUserStoryGridArrayAdapter
					.getItem(mUserStoryGridArrayAdapter.getCount() - 1);
			maxId = userPhoto.getTimeline_id() - 1;
		}
		if (maxId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return maxId;
		}
	}

	public long getSinceId() {
		long sinceId = 0;
		if (isListlayout && mUserStoryListArrayAdapter != null
				&& mUserStoryListArrayAdapter.getCount() > 0) {
			UserPhoto userPhoto = mUserStoryListArrayAdapter.getItem(0);
			sinceId = userPhoto.getTimeline_id() + 1;
		} else if (!isListlayout && mUserStoryGridArrayAdapter != null
				&& mUserStoryGridArrayAdapter.getCount() > 0) {
			UserPhoto userPhoto = mUserStoryGridArrayAdapter.getItem(0);
			sinceId = userPhoto.getTimeline_id() + 1;
		}
		if (sinceId == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return sinceId;
		}
	}

	private String getTypeFromExtras() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String type = extras.getString(EXTRA_USER_STORY_TYPE);
			return type;
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_story_new_grid);
		storyType = getTypeFromExtras();
		if (storyType.equals(STORY_TYPE_NEW)) {
			getSupportActionBar().setTitle(R.string.user_photo_new);
		} else {
			getSupportActionBar().setTitle(R.string.user_photo_lbs);
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				CommonUtilities.STORY_CONTENT_MESSAGE_ACTION));
		mAllUserPhotoList = new ArrayList<UserPhoto>();
		// 加载画面
		setupGridComponents();
		doRetrieveUserPhotoList(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.story_actions, mMenu);
		menuItemChangeType = mMenu.findItem(R.id.menu_item_story_change);
		return true;
	}

	@Override
	protected void onDestroy() {
		try {
			unregisterReceiver(mHandleMessageReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
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
		if (notMoreDataFound && !isUp) {
			Toast.makeText(this, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
		} else {
			doRetrieveUserPhotoList(isUp);
		}
	}

	private void setupComponents() {
		swypeLayout = (SwipeRefreshLayout) findViewById(R.id.swype);
		userStoryListView = (ListView) findViewById(R.id.activity_story_listView);
		emptyTextView = (TextView) findViewById(R.id.activity_story_listView_emptyview_text);
		emptyTextView.setVisibility(View.GONE);
		emptyTextView.setText("");
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		mUserStoryListArrayAdapter = new UserStoryListArrayAdapter(this,
				swypeLayout, R.layout.item_story_user_photo);
		userStoryListView.setAdapter(mUserStoryListArrayAdapter);
		userStoryListView.setEmptyView(emptyTextView);
	}

	private void setupGridComponents() {
		swypeLayout = (SwipeRefreshLayout) findViewById(R.id.swype);
		userStoryGridView = (GridView) findViewById(R.id.activity_story_gridView);
		emptyTextView = (TextView) findViewById(R.id.activity_story_gridView_emptyview_text);
		emptyTextView.setVisibility(View.GONE);
		emptyTextView.setText("");
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		mUserStoryGridArrayAdapter = new UserStoryGridArrayAdapter(this);
		userStoryGridView.setAdapter(mUserStoryGridArrayAdapter);
		userStoryGridView.setEmptyView(emptyTextView);
		userStoryGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserPhoto userPhoto = mUserStoryGridArrayAdapter
						.getItem(position);
				UserStoryListCursorAdapter.gotoStoryCommentActivity(
						UserStoryNewListActivity.this, userPhoto);
			}
		});
	}
}