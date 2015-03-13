package com.ruptech.chinatalk.ui.story;

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

import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.RetrieveMyChannelListTask;
import com.ruptech.chinatalk.utils.AppPreferences;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ChannelListArrayAdapter;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout;
import com.ruptech.chinatalk.widget.SwipeRefreshLayout.OnRefreshListener;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MyChannelListActivity extends ActionBarActivity implements
		OnRefreshListener {

	private final static String TAG = Utils.CATEGORY
			+ MyChannelListActivity.class.getSimpleName();

	@InjectView(R.id.activity_my_channel_emptyview_text)
	TextView emptyTextView;

	private static MyChannelListActivity instance = null;

	private GenericTask mRetrieveMyChannelListTask;

	private final TaskListener mRetrieveMyChannelListTaskListener = new TaskAdapter() {

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			RetrieveMyChannelListTask RetrieveMyChannelListTask = (RetrieveMyChannelListTask) task;
			if (result == TaskResult.OK) {
				List<Channel> myChannelList = RetrieveMyChannelListTask
						.getmyChannelList();
				if (myChannelList.size() < AppPreferences.PAGE_COUNT_20) {
					notMoreDataFound = true;
				} else {
					notMoreDataFound = false;
				}
				addAllStoryListArrayAdapter(myChannelList);
				swypeLayout.setRefreshing(false);
			} else {
				String msg = task.getMsg();
				swypeLayout.setRefreshing(false);

				if (!Utils.isEmpty(msg)) {
					Toast.makeText(MyChannelListActivity.this, msg,
							Toast.LENGTH_SHORT)
							.show();
				}
				finish();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			RetrieveMyChannelListTask RetrieveMyChannelListTask = (RetrieveMyChannelListTask) task;
			swypeLayout.setProgressTop(RetrieveMyChannelListTask.isTop());
			swypeLayout.setRefreshing(true);
		}

	};

	private ChannelListArrayAdapter mMyChannelListArrayAdapter;

	private boolean notMoreDataFound = false;

	@InjectView(R.id.swype)
	SwipeRefreshLayout swypeLayout;

	@InjectView(R.id.activity_my_channel_list)
	ListView myChannelListView;

	private void addAllStoryListArrayAdapter(List<Channel> myChannelList) {
		for (Channel channel : myChannelList) {
			mMyChannelListArrayAdapter.add(channel);
		}
		if (myChannelList.size() == 0) {
			emptyTextView.setText(getString(R.string.no_data_found));
		}
	}

	private void doRetrieveMyChannelList(boolean isTop) {
		if (notMoreDataFound
				|| (mRetrieveMyChannelListTask != null && mRetrieveMyChannelListTask
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
		mRetrieveMyChannelListTask = new RetrieveMyChannelListTask(isTop,
				maxId, sinceId);
		mRetrieveMyChannelListTask
				.setListener(mRetrieveMyChannelListTaskListener);
		mRetrieveMyChannelListTask.execute();
	}

	private int getContentViewRes() {
		return R.layout.activity_my_channel_list;
	}

	private long getMaxId() {
		if (mMyChannelListArrayAdapter.getCount() == 0) {
			return Long.MAX_VALUE;
		} else {
			return mMyChannelListArrayAdapter.getItem(
					mMyChannelListArrayAdapter.getCount() - 1).getId();
		}
	}

	private long getSinceId() {
		if (mMyChannelListArrayAdapter.getCount() == 0) {
			return AppPreferences.ID_IMPOSSIBLE;
		} else {
			return mMyChannelListArrayAdapter.getItem(0).getId();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewRes());
		instance = this;
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.my_channel);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setupComponents();

		doRetrieveMyChannelList(true);
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
		} else {
			doRetrieveMyChannelList(isTop);
		}
	}

	private void setupComponents() {
		swypeLayout.setOnRefreshListener(this);
		swypeLayout.setColorScheme(R.color.yellow, R.color.orange,
				R.color.chat_stranger_background, R.color.opacify_gray);
		myChannelListView.setEmptyView(emptyTextView);
		// Channel
		mMyChannelListArrayAdapter = new ChannelListArrayAdapter(this);
		myChannelListView.setAdapter(mMyChannelListArrayAdapter);
		myChannelListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Channel channel = mMyChannelListArrayAdapter.getItem(position);
				Intent intent = new Intent(MyChannelListActivity.this,
						ChannelPopularListActivity.class);
				intent.putExtra(ChannelPopularListActivity.EXTRA_CHNANEL,
						channel);
				startActivity(intent);
			}
		});
	}
}