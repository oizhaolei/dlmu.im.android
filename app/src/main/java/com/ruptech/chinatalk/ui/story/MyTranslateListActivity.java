package com.ruptech.chinatalk.ui.story;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.StoryTranslate;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveStoryTranslateListTask;
import com.ruptech.chinatalk.task.impl.RetrieveUserPhotoTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyTranslateListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MyTranslateListActivity extends ActionBarActivity implements
SwipeRefreshLayout.OnRefreshListener {

	private final static String TAG = Utils.CATEGORY
			+ MyTranslateListActivity.class.getSimpleName();

	@InjectView(R.id.activity_my_emptyview_text)
	TextView emptyTextView;
	private static MyTranslateListActivity instance = null;

	private GenericTask mRetrieveMyTranslateListTask;

	private final TaskListener mRetrieveMyTranslateListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveStoryTranslateListTask retrieveMyTranslateListTask = (RetrieveStoryTranslateListTask) task;
			if (result == TaskResult.OK) {
				List<StoryTranslate> myTranslateList = retrieveMyTranslateListTask
						.getStoryTranslateList();
				if (myTranslateList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				addAllStoryListArrayAdapter(myTranslateList);
				swypeLayout.setRefreshing(false);
			} else {
				String msg = task.getMsg();
				swypeLayout.setRefreshing(false);

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(MyTranslateListActivity.this, msg,
							Toast.LENGTH_SHORT)
							.show();
				}
				finish();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveStoryTranslateListTask retrieveMyCommentListTask = (RetrieveStoryTranslateListTask) task;
			swypeLayout.setProgressTop(retrieveMyCommentListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	private long userId;

	private MyTranslateListArrayAdapter mMyTranslateListArrayAdapter;

	private boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	private final BroadcastReceiver mHandleTranslateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			StoryTranslate storyTranslate = (StoryTranslate) intent.getExtras()
					.getSerializable(CommonUtilities.EXTRA_MESSAGE);
			if (storyTranslate != null) {

				mMyTranslateListArrayAdapter
							.updateTranslate(storyTranslate);
			}
		}
	};

	@InjectView(R.id.activity_my_listView)
	ListView myTranslateListView;

	private void addAllStoryListArrayAdapter(
			List<StoryTranslate> myTranslateList) {
		for (StoryTranslate storyTranslate : myTranslateList) {
			mMyTranslateListArrayAdapter.add(storyTranslate);
		}
		if (myTranslateList.size() == 0) {
			emptyTextView.setText(getString(R.string.no_data_found));
		}
	}

	private void doRetrieveMyTranslateList(boolean isTop) {
		if (notMoreDataFound
				|| (mRetrieveMyTranslateListTask != null && mRetrieveMyTranslateListTask
				.getStatus() == GenericTask.Status.RUNNING)) {
			return;
		}
		long sinceId;
		long maxId;
		if (isTop) {
			sinceId = getSinceId();
			maxId = Long.MAX_VALUE;
		} else {
			maxId = getMaxId();
			sinceId = AppPreferences.ID_IMPOSSIBLE;
		}
		mRetrieveMyTranslateListTask = new RetrieveStoryTranslateListTask(
				isTop, maxId, sinceId, -1, userId);
		mRetrieveMyTranslateListTask
				.setListener(mRetrieveMyTranslateListTaskListener);
		mRetrieveMyTranslateListTask.execute();
	}

	private int getContentViewRes() {
		return R.layout.activity_my_translate_list;
	}

	private long getMaxId() {
		if (mMyTranslateListArrayAdapter.getCount() == 0) {
			return Long.MAX_VALUE;
		} else {
			return mMyTranslateListArrayAdapter.getItem(
					mMyTranslateListArrayAdapter.getCount() - 1).getId() - 1;
		}
	}

	private long getSinceId() {
		if (mMyTranslateListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mMyTranslateListArrayAdapter.getItem(0).getId() + 1;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		instance = this;
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.my_translate);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		parseExtras(getIntent().getExtras());

		setupComponents();

		doRetrieveMyTranslateList(true);
		registerReceiver(mHandleTranslateReceiver, new IntentFilter(
				CommonUtilities.STORY_TRANSLATE_ACTION));
	}

	@Override
	protected void onDestroy() {

		try {
			unregisterReceiver(mHandleTranslateReceiver);
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
	public void onRefresh(boolean isTop) {
		swypeLayout.setRefreshing(false);
		if (notMoreDataFound && !isTop) {
			Toast.makeText(this, R.string.no_more_data,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void parseExtras(Bundle extras) {
		userId = extras.getLong(ProfileActivity.EXTRA_USER_ID);
	}
	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setEnabled(false);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		myTranslateListView.setEmptyView(emptyTextView);
		// comment
		mMyTranslateListArrayAdapter = new MyTranslateListArrayAdapter(this);
		myTranslateListView.setAdapter(mMyTranslateListArrayAdapter);
		myTranslateListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				StoryTranslate storyTranslate = mMyTranslateListArrayAdapter
						.getItem(position);

				RetrieveUserPhotoTask retrieveUserPhotoTask = new RetrieveUserPhotoTask(
						storyTranslate.getUser_photo_id(), App.readUser().lang);
				retrieveUserPhotoTask.setListener(new TaskAdapter() {
					@Override
					public void onPostExecute(GenericTask task,
							TaskResult result) {
						RetrieveUserPhotoTask retrieveUserPhotoTask = (RetrieveUserPhotoTask) task;
						if (result == TaskResult.OK) {
							UserPhoto userPhoto = retrieveUserPhotoTask
									.getUserPhoto();
							Intent intent = new Intent(
									MyTranslateListActivity.this,
									UserStoryTranslateActivity.class);
							intent.putExtra(
									UserStoryTranslateActivity.EXTRA_USER_PHOTO,
									userPhoto);

							startActivity(intent);
						}
					}
				});
				retrieveUserPhotoTask.execute();
			}
		});
		OnScrollListener onScrollListener = new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getLastVisiblePosition() == view.getCount() - 1
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					doRetrieveMyTranslateList(false);
				}
			}
		};
		myTranslateListView.setOnScrollListener(onScrollListener);
	}
}