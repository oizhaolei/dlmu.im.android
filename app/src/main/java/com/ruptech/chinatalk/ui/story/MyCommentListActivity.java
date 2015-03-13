package com.ruptech.chinatalk.ui.story;

import android.content.Intent;
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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.UserPhoto;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveMyCommentListTask;
import com.ruptech.chinatalk.ui.user.ProfileActivity;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.MyCommentListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MyCommentListActivity extends ActionBarActivity implements
SwipeRefreshLayout.OnRefreshListener {

	private final static String TAG = Utils.CATEGORY
			+ MyCommentListActivity.class.getSimpleName();

	@InjectView(R.id.activity_my_comment_emptyview_text)
	TextView emptyTextView;
	private static MyCommentListActivity instance = null;

	private GenericTask mRetrieveMyCommentListTask;

	private final TaskListener mRetrieveMyCommentListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveMyCommentListTask retrieveMyCommentListTask = (RetrieveMyCommentListTask) task;
			if (result == TaskResult.OK) {
				List<UserPhoto> myCommentList = retrieveMyCommentListTask
						.getMyCommentList();
				if (myCommentList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				addAllStoryListArrayAdapter(myCommentList);
				swypeLayout.setRefreshing(false);
			} else {
				String msg = task.getMsg();
				swypeLayout.setRefreshing(false);

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(MyCommentListActivity.this, msg,
							Toast.LENGTH_SHORT)
							.show();
				}
				finish();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveMyCommentListTask retrieveMyCommentListTask = (RetrieveMyCommentListTask) task;
			swypeLayout.setProgressTop(retrieveMyCommentListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	private long userId;

	private MyCommentListArrayAdapter mMyCommentListArrayAdapter;

	private boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_my_comment_listView)
	ListView myCommentListView;

	private void addAllStoryListArrayAdapter(List<UserPhoto> myCommentList) {
		for (UserPhoto userPhoto : myCommentList) {
			mMyCommentListArrayAdapter.add(userPhoto);
		}
		if (myCommentList.size() == 0) {
			emptyTextView.setText(getString(R.string.no_data_found));
		}
	}

	private void doRetrieveMyCommentList(boolean isTop) {
		if (notMoreDataFound
				|| (mRetrieveMyCommentListTask != null && mRetrieveMyCommentListTask
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
		mRetrieveMyCommentListTask = new RetrieveMyCommentListTask(isTop,
				maxId, sinceId, userId);
		mRetrieveMyCommentListTask
		.setListener(mRetrieveMyCommentListTaskListener);
		mRetrieveMyCommentListTask.execute();
	}

	private int getContentViewRes() {
		return R.layout.activity_my_comment_list;
	}

	private long getMaxId() {
		if (mMyCommentListArrayAdapter.getCount() == 0) {
			return Long.MAX_VALUE;
		} else {
			return mMyCommentListArrayAdapter.getItem(
					mMyCommentListArrayAdapter.getCount() - 1).getId() - 1;
		}
	}

	private long getSinceId() {
		if (mMyCommentListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mMyCommentListArrayAdapter.getItem(0).getId() + 1;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		instance = this;
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.my_comment);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		parseExtras(getIntent().getExtras());

		setupComponents();

		doRetrieveMyCommentList(true);
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
		myCommentListView.setEmptyView(emptyTextView);
		// comment
		mMyCommentListArrayAdapter = new MyCommentListArrayAdapter(this);
		myCommentListView.setAdapter(mMyCommentListArrayAdapter);
		myCommentListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				UserPhoto userPhoto = mMyCommentListArrayAdapter
						.getItem(position);
				Intent intent = new Intent(MyCommentListActivity.this,
						UserStoryCommentActivity.class);
				intent.putExtra(UserStoryCommentActivity.EXTRA_USER_PHOTO,
						userPhoto);
				intent.putExtra(
						UserStoryCommentActivity.EXTRA_IS_REAL_USERPHOTO, false);

				startActivity(intent);
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
					doRetrieveMyCommentList(false);
				}
			}
		};
		myCommentListView.setOnScrollListener(onScrollListener);
	}
}