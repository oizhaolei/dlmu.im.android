package com.ruptech.chinatalk.ui.story;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.chinatalk.App;
import com.ruptech.chinatalk.R;
import com.ruptech.chinatalk.model.Channel;
import com.ruptech.chinatalk.task.GenericTask;
import com.ruptech.chinatalk.task.TaskAdapter;
import com.ruptech.chinatalk.task.TaskListener;
import com.ruptech.chinatalk.task.TaskResult;
import com.ruptech.chinatalk.task.impl.ChannelFollowTask;
import com.ruptech.chinatalk.ui.fragment.SlidingTabLayout;
import com.ruptech.chinatalk.ui.user.ScrollTabHolder;
import com.ruptech.chinatalk.utils.CommonUtilities;
import com.ruptech.chinatalk.utils.Utils;
import com.ruptech.chinatalk.widget.ChannelPhotoListArrayAdapter;
import com.ruptech.chinatalk.widget.TapPagerAdapter;
import com.ruptech.chinatalk.widget.processbutton.ActionProcessButton;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ChannelPopularListActivity extends ActionBarActivity implements
		ScrollTabHolder, ViewPager.OnPageChangeListener {

	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

	private static ChannelPopularListActivity instance;

	public static void close() {
		if (instance != null) {
			instance.finish();
			instance = null;
		}
	}

	private final String TAG = Utils.CATEGORY
			+ ChannelPopularListActivity.class.getSimpleName();

	private static GenericTask mFollowChannelTask;

	private final TaskListener mFollowChannelTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			ChannelFollowTask channelFollowTask = (ChannelFollowTask) task;
			stopProgress();
			if (result == TaskResult.OK) {
				Channel resultChannel = channelFollowTask.getChannel();
				if (resultChannel != null) {
					channel.setIs_follower(resultChannel.getIs_follower());
					channel.setFollower_count(resultChannel.getFollower_count());
					channel.setPopular_count(resultChannel.getPopular_count());
					ChannelPhotoListArrayAdapter.changeLocalChannel(channel);

					displayChannel();
				}
			} else {
				Toast.makeText(ChannelPopularListActivity.this,
						channelFollowTask.getMsg(), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onPreExecute(GenericTask task) {
			startProgress();
		}

	};

	public static final String EXTRA_CHNANEL = "CHNANEL";
	private LayoutInflater mInflater;
	@InjectView(R.id.header)
	View headerView;
	@InjectView(R.id.activity_channel_title_textview)
	TextView mChannelTitleTextview;
	@InjectView(R.id.activity_channel_popular_textview)
	TextView mChannelPopularTextview;
	@InjectView(R.id.activity_channel_popular_fans_textview)
	TextView mChannelFansTextview;

	@InjectView(R.id.activity_channel_follow_buttom)
	ActionProcessButton mChannelfollowButtonview;

	Channel channel;

	TapPagerAdapter mPagerAdapter;

	@InjectView(R.id.pager)
	ViewPager mViewPager;

	@InjectView(R.id.sliding_tabs)
	SlidingTabLayout mTabLayout;

	public static final String CHANNEL_TYPE_NEW = "new";

	public static final String CHANNEL_TYPE_POPULAR = "top";

	private int mActionBarHeight;

	private final TypedValue mTypedValue = new TypedValue();

	private int mMinHeaderHeight;

	private final BroadcastReceiver mHandleChannelReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			channel = App.channelDAO.fetchChannel(channel.getId());
			displayChannel();
		}
	};

	private ProgressDialog progressDialog;

	@Override
	public void adjustScroll(int scrollHeight) {

	}

	private void displayChannel() {
		if (channel != null) {
			String title = channel.getTitle();
			mChannelTitleTextview.setText(title);
			mChannelPopularTextview.setText(String.valueOf(channel
					.getPopular_count()));
			mChannelFansTextview.setText(String.valueOf(channel
					.getFollower_count()));

			if (Integer.valueOf(channel.getIs_follower()) == 1) {
				// 取消关注
				mChannelfollowButtonview
						.setText(getString(R.string.friend_menu_delete));
			} else {
				// 关注
				mChannelfollowButtonview
						.setText(getString(R.string.add_as_friend));
			}
		}
	}

	public void doCamera(MenuItem item) {
		Intent intent = new Intent(this, PhotoAlbumActivity.class);
		intent.putExtra(UserStoryTagActivity.EXTRA_TAG, channel.getTitle());
		startActivity(intent);
	}

	public void doFollow(View v) {
		String follower = "true";
		if (channel != null && Integer.valueOf(channel.getIs_follower()) == 1) {
			follower = "false";
		}
		if (mFollowChannelTask != null
				&& mFollowChannelTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		}
		mFollowChannelTask = new ChannelFollowTask(channel.getId(), follower);
		mFollowChannelTask.setListener(mFollowChannelTaskListener);
		mFollowChannelTask.execute();
	}

	public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			getTheme().resolveAttribute(android.R.attr.actionBarSize,
					mTypedValue, true);
		} else {
			getTheme()
					.resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
		}

		mActionBarHeight = TypedValue.complexToDimensionPixelSize(
				mTypedValue.data, getResources().getDisplayMetrics());

		return mActionBarHeight;
	}

	public int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop() + 3;

		int headerHeight = 0;
		if (firstVisiblePosition >= 1) {
			headerHeight = headerView.getHeight();
		}

		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_popular_list);
		ButterKnife.inject(this);
		getSupportActionBar().setTitle(R.string.main_sub_tab_channel);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		instance = this;

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			// 获取channel_id
			if (extras != null) {
				channel = (Channel) extras.get(EXTRA_CHNANEL);
			}
		}
		// 加载画面
		setupComponents();
		displayChannel();
		registerReceiver(mHandleChannelReceiver, new IntentFilter(
				CommonUtilities.CHANNEL_ACTION));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu mMenu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shot_actions, mMenu);
		return true;
	}

	@Override
	protected void onDestroy() {
		instance = null;
		try {
			unregisterReceiver(mHandleChannelReceiver);
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
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter
				.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);
		if (currentHolder != null && headerView != null) {
			currentHolder
					.adjustScroll((int) (headerView.getHeight() + headerView
							.getTranslationY()));
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount, int pagePosition) {
		if (mViewPager.getCurrentItem() == pagePosition) {
			int scrollY = getScrollY(view);
			headerView.setTranslationY(Math.max(-scrollY, -mMinHeaderHeight));
		}
	}

	private void setupComponents() {
		mMinHeaderHeight = getResources().getDimensionPixelSize(
				R.dimen.channel_min_header_height);
		mPagerAdapter = new TapPagerAdapter(this, getSupportFragmentManager());
		mPagerAdapter.setTabHolderScrollingContent(this);
		mPagerAdapter.addTab(R.string.main_sub_tab_channel_hot,
				ChannelPopularFragment.class);
		mPagerAdapter.addTab(R.string.main_sub_tab_channel_new,
				ChannelNewFragment.class);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(2);

		mTabLayout.setViewPager(mViewPager);
		mTabLayout.setOnPageChangeListener(this);

	}

	private void startProgress() {
		mChannelfollowButtonview.setEnabled(false);
		progressDialog = Utils.showDialog(this,
				getString(R.string.please_waiting));
	}

	private void stopProgress() {
		mChannelfollowButtonview.setEnabled(true);
		Utils.dismissDialog(progressDialog);
	}
}